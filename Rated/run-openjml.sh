#!/usr/bin/env bash

set -u -o pipefail

cd "$(dirname "$(realpath "$0")")" || exit 1

classpath_file="/tmp/rated-openjml-classpath.txt"
esc_result_file="/tmp/rated-openjml-esc-results.log"
rac_result_file="/tmp/rated-openjml-rac-results.log"
status_file="/tmp/rated-openjml-exit.txt"
rac_classes_dir="target/openjml-rac-classes"

rm -f "$esc_result_file" "$rac_result_file" "$status_file"

echo "============================================================"
echo " OpenJML - verifiche ESC e RAC del progetto Rated"
echo "============================================================"
echo

if ! command -v openjml >/dev/null 2>&1; then
    echo "ERRORE: OpenJML non e' presente nel PATH di WSL."
    exit 127
fi

if ! command -v mvn >/dev/null 2>&1; then
    echo "ERRORE: Maven non e' presente nel PATH di WSL."
    exit 127
fi

echo "OpenJML: $(command -v openjml)"
openjml --version
echo
echo "Risoluzione del classpath Maven..."

rm -f "$classpath_file"
if ! mvn -q -DincludeScope=test dependency:build-classpath \
    -Dmdep.outputFile="$classpath_file"; then
    echo "ERRORE: Maven non ha generato il classpath."
    exit 1
fi

if [[ ! -s "$classpath_file" ]]; then
    echo "ERRORE: il classpath Maven e' vuoto."
    exit 1
fi

echo "Compilazione delle classi di produzione..."
if ! mvn -q -DskipTests compile; then
    echo "ERRORE: Maven non ha compilato il progetto."
    exit 1
fi

mapfile -t jml_files < <(
    grep -rl --include='*.java' -e '//@' -e '/\*@' src/main/java | sort
)

if (( ${#jml_files[@]} == 0 )); then
    echo "Nessun file Java con annotazioni JML trovato."
    exit 0
fi

echo
echo "File con annotazioni JML trovati: ${#jml_files[@]}"
printf '  - %s\n' "${jml_files[@]}"
echo
echo "Avvio della verifica ESC..."
echo "============================================================"
echo

dependency_classpath="$(<"$classpath_file")"
openjml --esc --split --timeout=40 -proc:none \
    -sourcepath src/main/java \
    -cp "target/classes:$dependency_classpath" \
    "${jml_files[@]}" 2>&1 \
    | sed -u \
        -e '/^Skipping proof attempt for split *$/d' \
        -e '/^No matching splits$/d' \
        -e '/^    model\.Entity\..* ==> model\.Entity\./d' \
        -e '/^    DECL model\.Entity\./d' \
    | tee "$esc_result_file"
esc_status=${PIPESTATUS[0]}

echo
echo "============================================================"
if (( esc_status == 0 )); then
    echo "RISULTATO ESC: verifica statica completata senza problemi."
else
    echo "RISULTATO ESC: OpenJML ha segnalato errori o verifiche fallite."
fi
echo "Codice di uscita ESC: $esc_status"
echo "============================================================"

openjml_executable="$(readlink -f "$(command -v openjml)")"
openjml_home="$(dirname "$openjml_executable")"
jmlruntime_jar="$openjml_home/jmlruntime.jar"

if [[ ! -f "$jmlruntime_jar" ]]; then
    echo "ERRORE: jmlruntime.jar non trovato accanto a OpenJML: $jmlruntime_jar"
    exit 1
fi

echo
echo "Preparazione delle classi strumentate per RAC..."
rm -rf "$rac_classes_dir"
mkdir -p "$rac_classes_dir"
cp -a target/classes/. "$rac_classes_dir/"

openjml --rac -proc:none \
    -d "$rac_classes_dir" \
    -sourcepath src/main/java \
    -cp "target/classes:$dependency_classpath:$jmlruntime_jar" \
    "${jml_files[@]}" 2>&1 \
    | tee "$rac_result_file"
rac_compile_status=${PIPESTATUS[0]}

rac_status=$rac_compile_status

if (( esc_status == 0 && rac_status == 0 )); then
    overall_status=0
else
    overall_status=1
fi

printf '%s\n' "$overall_status" >"$status_file"

echo
echo "============================================================"
if (( rac_compile_status != 0 )); then
    echo "RISULTATO RAC: compilazione strumentata fallita."
else
    echo "RISULTATO RAC: strumentazione completata senza problemi."
fi
echo "Codice di uscita RAC: $rac_status"
echo "Codice di uscita complessivo: $overall_status"
echo "============================================================"

exit "$overall_status"

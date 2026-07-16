#!/usr/bin/env bash

set -u -o pipefail

cd "$(dirname "$(realpath "$0")")" || exit 1

classpath_file="/tmp/rated-openjml-classpath.txt"
result_file="/tmp/rated-openjml-results.log"
status_file="/tmp/rated-openjml-exit.txt"

rm -f "$result_file" "$status_file"

echo "============================================================"
echo " OpenJML - verifica ESC del progetto Rated"
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
    | tee "$result_file"
openjml_status=${PIPESTATUS[0]}

printf '%s\n' "$openjml_status" >"$status_file"

echo
echo "============================================================"
if (( openjml_status == 0 )); then
    echo "RISULTATO: verifica OpenJML completata senza problemi."
else
    echo "RISULTATO: OpenJML ha segnalato errori o verifiche fallite."
fi
echo "Codice di uscita: $openjml_status"
echo "============================================================"

exit "$openjml_status"

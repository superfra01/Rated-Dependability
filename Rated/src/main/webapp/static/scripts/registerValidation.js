const emailPattern = /^[a-zA-Z0-9.%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
const usernamePattern = /^[a-zA-Z0-9.]{3,30}$/;
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%?&.])[A-Za-z\d@$!%?&.]{8,64}$/;

const messages = {
    email: "Inserire un indirizzo email valido nella forma: name@domain.ext",
    username: "Lo username può contenere solo lettere, numeri o punti ed essere lungo tra 3 e 30 caratteri",
    password: "La password deve contenere tra 8 e 64 caratteri, almeno una lettera minuscola, una maiuscola, un numero e un carattere speciale (@, $, !, %, ?, &, .)",
    confirmPassword: "Le password non corrispondono",
    profileIcon: "Seleziona un'icona valida (formato immagine, non un altro tipo di file)",
    bio: "La biografia non può essere vuota",
    genres: "Seleziona almeno 2 generi preferiti",
    backendError: "Registrazione impossibile: lista generi non disponibile"
};

let showErrors = false;

function validateFormElem(formElem, pattern, message) {
    const span = document.getElementById("error" + capitalize(formElem.name));
    if (showErrors) {
        if (!formElem.value.match(pattern)) {
            setError(formElem, span, message);
            return false;
        }
        clearError(formElem, span);
    }
    return true;
}

// Funzione specifica per validare i generi
function validateGenres() {
    const genresContainer = document.getElementById("genresContainer");
    const span = document.getElementById("errorGenres");
    
    // Recupero tutte le checkbox presenti
    const allCheckboxes = document.querySelectorAll('input[name="genres"]');
    
    // Se non ci sono checkbox, significa che il backend ha passato null o lista vuota
    if (allCheckboxes.length === 0) {
        if (showErrors) {
            genresContainer.classList.add("error");
            span.innerHTML = messages.backendError;
            span.style.color = "red";
        }
        return false;
    }

    // Conto quante sono selezionate
    const checkedCount = document.querySelectorAll('input[name="genres"]:checked').length;

    if (showErrors) {
        if (checkedCount < 2) {
            genresContainer.classList.add("error");
            span.innerHTML = messages.genres;
            span.style.color = "red";
            return false;
        } else {
            genresContainer.classList.remove("error");
            span.innerHTML = "";
            return true;
        }
    }
    return true;
}

function validate() {
    let valid = true;
    const form = document.getElementById("regForm");

    valid = validateFormElem(form.username, usernamePattern, messages.username) && valid;
    valid = validateFormElem(form.email, emailPattern, messages.email) && valid;
    valid = validateFormElem(form.password, passwordPattern, messages.password) && valid;

    const confirmPassword = form.confirm_password;
    if (showErrors && confirmPassword.value !== form.password.value) {
        setError(confirmPassword, document.getElementById("errorConfirmPassword"), messages.confirmPassword);
        valid = false;
    } else if (showErrors) {
        clearError(confirmPassword, document.getElementById("errorConfirmPassword"));
    }

    const profileIcon = form.profile_icon;
    const spanProfileIcon = document.getElementById("errorProfileIcon");
    if (showErrors && (!profileIcon.files[0] || !profileIcon.files[0].type.startsWith("image/"))) {
        setError(profileIcon, spanProfileIcon, messages.profileIcon);
        valid = false;
    } else if (showErrors) {
        clearError(profileIcon, spanProfileIcon);
    }

    const bio = form.bio;
    const spanBio = document.getElementById("errorBio");
    if (showErrors && !bio.value.trim()) {
        setError(bio, spanBio, messages.bio);
        valid = false;
    } else if (showErrors) {
        clearError(bio, spanBio);
    }

    // Validazione Generi
    if (!validateGenres()) {
        valid = false;
    }

    return valid;
}

function setError(input, span, message) {
    input.classList.add("error");
    span.innerHTML = message;
    span.style.color = "red";
}

function clearError(input, span) {
    input.classList.remove("error");
    span.innerHTML = "";
}

function capitalize(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}

document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("regForm");

    form.onsubmit = function (event) {
        showErrors = true;
        if (!validate()) {
            event.preventDefault();
        }
    };

    // Aggiungo listener "change" sulle checkbox per togliere l'errore in tempo reale
    const genreCheckboxes = document.querySelectorAll('input[name="genres"]');
    genreCheckboxes.forEach(box => {
        box.addEventListener('change', function() {
            if(showErrors) validateGenres();
        });
    });
});
function openOverlay(overlayId) {
    document.getElementById(overlayId).style.display = 'flex';
}

function closeOverlay(overlayId) {
    document.getElementById(overlayId).style.display = 'none';
}

function validatePasswordChangeForm() {
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        document.getElementById('passwordError').style.display = 'block';
        return false;
    }

    document.getElementById('passwordError').style.display = 'none';
    return true;
}

function validateProfileForm() {
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmProfilePassword').value;

    if (password !== confirmPassword) {
        document.getElementById('profilePasswordError').style.display = 'block';
        return false;
    }

    document.getElementById('profilePasswordError').style.display = 'none';
    return true;
}

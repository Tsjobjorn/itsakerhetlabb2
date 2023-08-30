function updateElements() {
    // AJAX request som hämtar data från /api/brute-force-statistics
    fetch('/api/brute-force-statistics')
        .then(response => response.json())
        .then(data => {
            // Uppdaterar HTML-elementen med data från /api/brute-force-statistics
            document.getElementById("failedAttempts").textContent = data.failedAttempts;
            const userStatusSpan = document.getElementById("userStatus");
            // Uppdaterar textfärgen baserat på om användaren är compromised eller inte
            // '?' Är en ternary operator som fungerar som en if-sats. Om data.userCompromised är true så sätts textfärgen till röd, annars till grön.
            userStatusSpan.textContent = data.userCompromised ? 'User has been compromised' : 'User has not been compromised';
            userStatusSpan.style.color = data.userCompromised ? 'red' : 'green';
        })
        .catch(error => {
            console.error("Error fetching brute-force statistics: ", error);
        });
}

// Uppdaterar HTML-elementen varje 10 millisekunder
setInterval(updateElements, 10); // 1000 milliseconds (1 second)
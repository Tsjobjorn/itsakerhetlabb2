let timerInterval; // To store the interval ID
let startTime; // To store the start time
let finalTime; // To store the final time

function startTimer() {
    startTime = Date.now();
    timerInterval = setInterval(updateTimer, 1000);
}

function updateTimer() {

    const elapsedTime = Math.floor((Date.now() - startTime) / 1000);
    document.getElementById('timer').textContent = `${elapsedTime}s`;
    finalTime = elapsedTime;
    console.log(finalTime);

}

function stopTimer() {
    clearInterval(timerInterval);
}

document.getElementById('startButton1').addEventListener('click', function() {
    startTimer();
});

document.getElementById('startButton2').addEventListener('click', function() {
    startTimer();
});

document.getElementById('startButton3').addEventListener('click', function() {
    startTimer();
});

document.getElementById('stopButton1').addEventListener('click', function() {
    stopTimer();
});

document.getElementById('stopButton2').addEventListener('click', function() {
    stopTimer();
});
var stompClient = null;
var username = null;
var room = null;
var accessToken = null;

// Function to connect to WebSocket
function connect() {
    room = document.getElementById("room").value;

    if (!room) {
        alert("Please enter a room name.");
        return;
    }

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({'Authorization': 'Bearer ' + accessToken}, function (frame) {
        console.log('Connected: ' + frame);

        document.getElementById("chat-container").style.display = "block";
        document.getElementById("roomName").innerText = room; // Set the room name in the UI

        stompClient.subscribe('/topic/room-' + room, function (message) {
            showMessage(JSON.parse(message.body)); // Pass the entire message object
        });

        stompClient.send("/app/chat.addUser",
            {},
            JSON.stringify({ sender: username, type: 'JOIN', room: room })
        );
    }, function (error) { // Add error callback
        console.error("WebSocket connection error:", error);
        alert("Could not connect to WebSocket.  Check console for details.");
    });
}

// Function to send a message
function sendMessage() {
    var messageContent = document.getElementById("message").value;

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            room: room
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        document.getElementById("message").value = "";
    }
}

// Function to display messages
function showMessage(message) {
    var messageArea = document.getElementById("messages");
    var messageElement = document.createElement("div");
    messageElement.classList.add("message");

    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        // Handle join/leave messages differently (e.g., display as system messages)
        messageElement.textContent = message.sender + " " + (message.type === 'JOIN' ? "joined!" : "left!");
        messageElement.classList.add("system-message"); // Add a class for styling
    } else {
        // Display regular chat messages
        const senderElement = document.createElement("div");
        senderElement.classList.add("sender");
        senderElement.textContent = message.sender;

        const contentElement = document.createElement("div");
        contentElement.textContent = message.content;

        messageElement.appendChild(senderElement);
        messageElement.appendChild(contentElement);

        if (message.sender === username) {
            messageElement.classList.add("sent");
        } else {
            messageElement.classList.add("received");
        }
    }


    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

// Function to handle user login
function login() {
    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    fetch('/api/user/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email: email, password: password })
    })
        .then(response => response.json())
        .then(data => {
            accessToken = data.accessToken;
            username = email; // Or get the username from the login response if available
            document.getElementById("login-signup-container").style.display = "none";
            document.getElementById("chat-container").style.display = "block";

            //Set Room Name
            document.getElementById("roomName").innerText = room;
            console.log('Login successful. Access Token:', accessToken);

            // Store the token (e.g., in localStorage)
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('username', username);

            // The room input should be visible *before* connecting
            // connect();  // Move connect() to be called after the user enters a room
        })
        .catch(error => {
            console.error('Login error:', error);
            alert('Login failed. Check your credentials.');
        });
}

// Function to handle user signup
function signup() {
    const email = document.getElementById("signupEmail").value;
    const password = document.getElementById("signupPassword").value;
    const nickname = document.getElementById("signupNickname").value;
    const phoneNumber = document.getElementById("signupPhoneNumber").value;
    const name = document.getElementById("signupName").value;

    fetch('/api/user/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email: email, password: password, nickname: nickname, phoneNumber: phoneNumber, name: name })
    })
        .then(response => response.json())
        .then(data => {
            alert('Signup successful!');
            // Optionally, automatically log the user in after signup
            document.getElementById("loginEmail").value = email;
            document.getElementById("loginPassword").value = password;
            login();
        })
        .catch(error => {
            console.error('Signup error:', error);
            alert('Signup failed. Please try again.');
        });
}

// Function to handle logout
function logout() {
    // Clear the stored token and username
    localStorage.removeItem('accessToken');
    localStorage.removeItem('username');
    accessToken = null;
    username = null;

    // Show the login/signup container and hide the chat container
    document.getElementById("login-signup-container").style.display = "block";
    document.getElementById("chat-container").style.display = "none";
}

// On page load, check if there's a token in localStorage
window.onload = function() {
    const storedToken = localStorage.getItem('accessToken');
    const storedUsername = localStorage.getItem('username');

    if (storedToken && storedUsername) {
        accessToken = storedToken;
        username = storedUsername;
        document.getElementById("login-signup-container").style.display = "none";
        document.getElementById("chat-container").style.display = "block";
    }
};

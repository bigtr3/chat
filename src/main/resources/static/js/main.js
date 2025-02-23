var stompClient = null;
var username = null;
var room = null;
var accessToken = null;

function connect() {
    username = document.getElementById("username").value;
    room = document.getElementById("room").value;
    accessToken = document.getElementById("accessToken").value;

    if (!username || !room || !accessToken) {
        alert("Please enter a username, room name, and access token.");
        return;
    }

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({'Authorization': 'Bearer ' + accessToken}, function (frame) { // Include the token
        console.log('Connected: ' + frame);

        document.getElementById("connect-container").style.display = "none";
        document.getElementById("chat-container").style.display = "block";


        stompClient.subscribe('/topic/room-' + room, function (message) {
            showMessage(JSON.parse(message.body).sender + ": " + JSON.parse(message.body).content);
        });

        stompClient.send("/app/chat.addUser",
            {},
            JSON.stringify({ sender: username, type: 'JOIN', room: room })
        );
    });
}

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

function showMessage(message) {
    var messageArea = document.getElementById("messages");
    var p = document.createElement("p");
    p.appendChild(document.createTextNode(message));
    messageArea.appendChild(p);
    messageArea.scrollTop = messageArea.scrollHeight; // Auto-scroll to bottom
}

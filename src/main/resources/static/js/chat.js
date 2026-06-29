let stompClient = null;
let currentChatRoomId = null;

const WS_ENDPOINT = "/ws";

const API = {
    createChatRoom: "/api/v1/chatrooms",
    chatRooms: "/api/v1/chatrooms",
    messages: (chatRoomId) => `/api/v1/chatrooms/${chatRoomId}/messages`,
    readMessages: (chatRoomId) => `/api/v1/chatrooms/${chatRoomId}/messages/read`,
    leaveChatRoom: (chatRoomId) => `/api/v1/chatrooms/${chatRoomId}`
};

const STOMP = {
    publish: (chatRoomId) => `/pub/chatrooms/${chatRoomId}`,
    subscribe: (chatRoomId) => `/sub/chatrooms/${chatRoomId}/messages`
};

document.addEventListener("DOMContentLoaded", () => {
    const path = location.pathname;

    if (path.includes("chat.html")) {
        initChatPage();
    }
});

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

function requireLogin() {
    const token = getAccessToken();

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return null;
    }

    return token;
}

async function createChatRoom(saleId) {
    const token = requireLogin();

    if (!token) {
        return;
    }

    try {
        const response = await fetch(API.createChatRoom, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                saleId: saleId
            })
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "채팅방 생성에 실패했습니다.");
            return;
        }

        const chatRoomId =
            result.data?.chatRoomId ||
            result.data?.id ||
            result.chatRoomId ||
            result.id;

        if (!chatRoomId) {
            alert("채팅방 ID를 확인할 수 없습니다.");
            return;
        }

        location.href = `./chat.html?roomId=${chatRoomId}`;

    } catch (error) {
        console.error(error);

        // 백엔드 채팅 API가 아직 완성되지 않았을 때 화면 확인용
        alert("채팅 API 연결 전이므로 예시 채팅방으로 이동합니다.");
        location.href = "./chat.html?roomId=1";
    }
}

async function initChatPage() {
    const token = requireLogin();

    if (!token) {
        return;
    }

    const params = new URLSearchParams(location.search);
    currentChatRoomId = params.get("roomId") || "1";

    await loadChatRooms();
    await loadMessages(currentChatRoomId);
    connectWebSocket(currentChatRoomId);
    markMessagesAsRead(currentChatRoomId);
}

async function loadChatRooms() {
    const token = getAccessToken();
    const list = document.getElementById("chatRoomList");

    if (!list) {
        return;
    }

    try {
        const response = await fetch(API.chatRooms, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSampleChatRooms();
            return;
        }

        const chatRooms = result.data || [];
        renderChatRooms(chatRooms);

    } catch (error) {
        console.error(error);
        renderSampleChatRooms();
    }
}

function renderChatRooms(chatRooms) {
    const list = document.getElementById("chatRoomList");

    if (!chatRooms.length) {
        renderSampleChatRooms();
        return;
    }

    list.innerHTML = chatRooms.map(room => `
        <div class="chat-room-item ${String(room.chatRoomId) === String(currentChatRoomId) ? "active" : ""}"
             onclick="moveChatRoom(${room.chatRoomId})">
            <div class="chat-avatar">${getInitial(room.opponentName || "상대")}</div>
            <div>
                <strong>${room.opponentName || "상대방"}</strong>
                <p>${room.lastMessage || "새로운 채팅방입니다."}</p>
            </div>
            ${room.unreadCount > 0 ? `<span class="unread-count">${room.unreadCount}</span>` : ""}
        </div>
    `).join("");
}

function renderSampleChatRooms() {
    const list = document.getElementById("chatRoomList");

    list.innerHTML = `
        <div class="chat-room-item active">
            <div class="chat-avatar">김</div>
            <div>
                <strong>김민혁</strong>
                <p>안녕하세요~</p>
            </div>
            <span class="unread-count">1</span>
        </div>

        <div class="chat-room-item">
            <div class="chat-avatar">이</div>
            <div>
                <strong>이지현</strong>
                <p>CU 상품 문의드립니다.</p>
            </div>
        </div>
    `;
}

function moveChatRoom(chatRoomId) {
    location.href = `./chat.html?roomId=${chatRoomId}`;
}

async function loadMessages(chatRoomId) {
    const token = getAccessToken();

    try {
        const response = await fetch(API.messages(chatRoomId), {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSampleMessages();
            return;
        }

        const messages = result.data || [];
        renderMessages(messages);

    } catch (error) {
        console.error(error);
        renderSampleMessages();
    }
}

function renderMessages(messages) {
    const messageList = document.getElementById("messageList");

    if (!messages.length) {
        messageList.innerHTML = "";
        return;
    }

    messageList.innerHTML = messages.map(message => createMessageHtml(message)).join("");
    scrollToBottom();
}

function renderSampleMessages() {
    const messageList = document.getElementById("messageList");

    messageList.innerHTML = `
        <div class="message-row me">
            <div>
                <div class="message-bubble">안녕하세요! 혹시 지금 상품 판매중인가요?</div>
                <span class="message-time">14:30</span>
            </div>
        </div>

        <div class="message-row other">
            <div class="chat-avatar">김</div>
            <div>
                <div class="message-bubble">안녕하세요~ 네 가능합니다!</div>
                <span class="message-time">14:31</span>
            </div>
        </div>
    `;

    scrollToBottom();
}

function connectWebSocket(chatRoomId) {
    if (!window.SockJS || !window.StompJs) {
        console.warn("STOMP 라이브러리를 불러오지 못했습니다.");
        return;
    }

    stompClient = new StompJs.Client({
        webSocketFactory: () => new SockJS(WS_ENDPOINT),
        connectHeaders: {
            Authorization: `Bearer ${getAccessToken()}`
        },
        debug: () => {},
        reconnectDelay: 5000,
        onConnect: () => {
            stompClient.subscribe(STOMP.subscribe(chatRoomId), (message) => {
                const receivedMessage = JSON.parse(message.body);
                appendMessage(receivedMessage);
            });
        },
        onStompError: (frame) => {
            console.error("STOMP ERROR", frame);
        }
    });

    stompClient.activate();
}

function sendMessage() {
    const input = document.getElementById("messageInput");
    const content = input.value.trim();

    if (!content) {
        return;
    }

    const message = {
        chatRoomId: Number(currentChatRoomId),
        content: content
    };

    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination: STOMP.publish(currentChatRoomId),
            headers: {
                Authorization: `Bearer ${getAccessToken()}`
            },
            body: JSON.stringify(message)
        });
    } else {
        appendMessage({
            senderType: "ME",
            content: content,
            createdAt: new Date().toISOString()
        });
    }

    input.value = "";
}

function handleMessageEnter(event) {
    if (event.key === "Enter") {
        sendMessage();
    }
}

function appendMessage(message) {
    const messageList = document.getElementById("messageList");

    messageList.insertAdjacentHTML("beforeend", createMessageHtml(message));
    scrollToBottom();
}

function createMessageHtml(message) {
    const isMe =
        message.senderType === "ME" ||
        message.isMine === true ||
        message.mine === true;

    const time = formatTime(message.createdAt || new Date().toISOString());

    if (isMe) {
        return `
            <div class="message-row me">
                <div>
                    <div class="message-bubble">${escapeHtml(message.content)}</div>
                    <span class="message-time">${time}</span>
                </div>
            </div>
        `;
    }

    return `
        <div class="message-row other">
            <div class="chat-avatar">${getInitial(message.senderName || "상대")}</div>
            <div>
                <div class="message-bubble">${escapeHtml(message.content)}</div>
                <span class="message-time">${time}</span>
            </div>
        </div>
    `;
}

async function markMessagesAsRead(chatRoomId) {
    const token = getAccessToken();

    try {
        await fetch(API.readMessages(chatRoomId), {
            method: "PATCH",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
    } catch (error) {
        console.error(error);
    }
}

async function leaveChatRoom() {
    const token = getAccessToken();

    if (!currentChatRoomId) {
        alert("채팅방 정보를 찾을 수 없습니다.");
        return;
    }

    if (!confirm("채팅방을 나가시겠습니까?")) {
        return;
    }

    try {
        const response = await fetch(API.leaveChatRoom(currentChatRoomId), {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        if (!response.ok) {
            alert("채팅방 나가기에 실패했습니다.");
            return;
        }

        alert("채팅방을 나갔습니다.");
        location.href = "./index.html";

    } catch (error) {
        console.error(error);
        alert("서버와 연결할 수 없습니다.");
    }
}

function scrollToBottom() {
    const messageList = document.getElementById("messageList");

    if (messageList) {
        messageList.scrollTop = messageList.scrollHeight;
    }
}

function formatTime(value) {
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toLocaleTimeString("ko-KR", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

function getInitial(name) {
    return name ? name.charAt(0) : "?";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

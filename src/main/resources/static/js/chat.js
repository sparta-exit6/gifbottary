let stompClient = null;
let currentChatRoomId = null;
let currentUserId = null;

const WS_ENDPOINT = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + "/ws/chat";

const API = {
    createChatRoom: "/api/v1/chatrooms",
    chatRooms: "/api/v1/chatrooms",
    messages: (chatRoomId) => `/api/v1/chatrooms/${chatRoomId}/messages`,
    readMessages: (chatRoomId) => `/api/v1/chatrooms/${chatRoomId}/messages/read`,
    leaveChatRoom: (chatRoomId) => `/api/v1/chatrooms/${chatRoomId}/members`,
    me: "/api/v1/auth/me"
};

const STOMP = {
    publish: () => `/pub/chat/message`,
    subscribe: (chatRoomId) => `/sub/chat/${chatRoomId}`
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
            result.data?.roomId ||
            result.data?.chatRoomId ||
            result.data?.id ||
            result.roomId ||
            result.chatRoomId ||
            result.id;

        if (!chatRoomId) {
            alert("채팅방 ID를 확인할 수 없습니다.");
            return;
        }

        location.href = `./chat.html?roomId=${chatRoomId}`;

    } catch (error) {
        console.error(error);
        alert("서버 연결에 실패하여 채팅방을 생성할 수 없습니다.");
    }
}

async function initChatPage() {
    const token = requireLogin();

    if (!token) {
        return;
    }

    await loadCurrentUserId();

    const params = new URLSearchParams(location.search);
    currentChatRoomId = params.get("roomId") || "1";

    await loadChatRooms();
    await loadMessages(currentChatRoomId);
    connectWebSocket(currentChatRoomId);
    markMessagesAsRead(currentChatRoomId);
}

async function loadCurrentUserId() {
    const token = getAccessToken();
    try {
        const res = await fetch(API.me, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        const json = await res.json();
        if (res.ok && json.success !== false && json.data) {
            currentUserId = json.data.userId || json.data.id;
        }
    } catch (e) {
        console.error("내 정보 조회 실패:", e);
    }
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
            list.innerHTML = `<div style="padding: 20px; text-align: center; color: #888;">채팅방 목록을 불러오지 못했습니다.</div>`;
            return;
        }

        const chatRooms = result.data || [];
        renderChatRooms(chatRooms);

    } catch (error) {
        console.error(error);
        list.innerHTML = `<div style="padding: 20px; text-align: center; color: #888;">서버 연결에 실패했습니다.</div>`;
    }
}

function renderChatRooms(chatRooms) {
    const list = document.getElementById("chatRoomList");

    if (!chatRooms.length) {
        list.innerHTML = `<div style="padding: 20px; text-align: center; color: #888;">참여 중인 채팅방이 없습니다.</div>`;
        return;
    }

    list.innerHTML = chatRooms.map(room => {
        const roomId = room.roomId || room.chatRoomId;
        const opponentName = room.otherUserName || room.opponentName || "상대방";
        const lastMsg = room.lastMessageContent || room.lastMessage || "새로운 채팅방입니다.";
        const unread = room.unreadCount || 0;
        return `
        <div class="chat-room-item ${String(roomId) === String(currentChatRoomId) ? "active" : ""}"
             onclick="moveChatRoom(${roomId})">
            <div class="chat-avatar">${getInitial(opponentName)}</div>
            <div>
                <strong>${opponentName}</strong>
                <p>${lastMsg}</p>
            </div>
            ${unread > 0 ? `<span class="unread-count">${unread}</span>` : ""}
        </div>
    `;
    }).join("");

    const activeRoom = chatRooms.find(r => String(r.roomId || r.chatRoomId) === String(currentChatRoomId)) || chatRooms[0];
    if (activeRoom) {
        renderProductBox(activeRoom);
    }
}

function renderProductBox(room) {
    if (!room) return;
    const nameEl = document.getElementById("chatProductName");
    const priceEl = document.getElementById("chatProductPrice");
    const statusEl = document.getElementById("chatProductStatus");
    const viewBtn = document.getElementById("chatProductViewBtn");
    const buyBtn = document.getElementById("chatProductBuyBtn");

    if (nameEl) nameEl.textContent = room.productName || "상품 정보 없음";
    if (priceEl) priceEl.textContent = room.salePrice ? `판매가 ${room.salePrice.toLocaleString()}원` : "판매가 -원";
    if (statusEl) {
        const statusMap = {
            "ON_SALE": "판매중",
            "SOLD_OUT": "판매완료",
            "PENDING_REVIEW": "검수중",
            "CANCELLED": "판매취소"
        };
        statusEl.textContent = statusMap[room.saleStatus] || room.saleStatus || "판매중";
    }
    if (viewBtn && room.saleId) {
        viewBtn.onclick = () => { location.href = `./product-detail.html?saleId=${room.saleId}`; };
    }
    if (buyBtn && room.saleId) {
        buyBtn.onclick = () => { location.href = `./product-detail.html?saleId=${room.saleId}`; };
    }
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
            console.warn("메시지 조회 실패:", result.message);
            return;
        }

        const messages = result.data || [];
        renderMessages(messages);

    } catch (error) {
        console.error("메시지 조회 에러:", error);
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

function connectWebSocket(chatRoomId) {
    if (!window.StompJs) {
        console.warn("STOMP 라이브러리를 불러오지 못했습니다.");
        return;
    }

    stompClient = new StompJs.Client({
        brokerURL: WS_ENDPOINT,
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
        roomId: Number(currentChatRoomId),
        content: content
    };

    if (stompClient && stompClient.connected) {
        stompClient.publish({
            destination: STOMP.publish(),
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
        (currentUserId && String(message.senderId) === String(currentUserId)) ||
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
    // 백엔드 웹소켓 진입 시 자동 읽음 처리되므로 HTTP 호출을 수행하지 않습니다.
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

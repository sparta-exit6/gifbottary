document.addEventListener("DOMContentLoaded", () => {
    loadMyInfo();
});

async function loadMyInfo() {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return;
    }

    try {
        const response = await fetch("/api/v1/auth/me", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "내 정보 조회에 실패했습니다.");
            location.href = "./login.html";
            return;
        }

        renderMyInfo(result.data);

    } catch (error) {
        console.error(error);

        // 백엔드 내 정보 조회 API 연결 전 화면 확인용 예시 데이터
        renderMyInfo({
            userId: 1,
            email: "test@example.com",
            name: "홍길동",
            role: "USER",
            pointBalance: 12000
        });
    }
}

function renderMyInfo(user) {
    document.getElementById("userId").textContent = user.userId ?? user.id ?? "-";
    document.getElementById("userEmail").textContent = user.email ?? "-";
    document.getElementById("userName").textContent = user.name ?? "-";
    document.getElementById("userRole").textContent = user.role ?? "-";
    document.getElementById("pointBalance").textContent = `${formatNumber(user.pointBalance ?? 0)} P`;

    document.getElementById("sidebarName").textContent = user.name ?? "사용자";
    document.getElementById("sidebarEmail").textContent = user.email ?? "-";

    const roleBadge = document.getElementById("roleBadge");
    roleBadge.textContent = user.role ?? "USER";

    if (user.role === "ADMIN") {
        roleBadge.classList.add("admin");
    }
}

function formatNumber(value) {
    return Number(value).toLocaleString("ko-KR");
}
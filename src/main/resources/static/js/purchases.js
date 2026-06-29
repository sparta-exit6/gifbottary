let allPurchases = [];
let currentFilter = "ALL";
let selectedPurchase = null;
let selectedRefundPurchase = null;

const PURCHASE_API = {
    list: "/api/v1/purchases/me",
    detail: (purchaseId) => `/api/v1/purchases/${purchaseId}`,
    revealPin: (purchaseId) => `/api/v1/purchases/${purchaseId}/reveal-pin`,
    refund: "/api/v1/refunds"
};

document.addEventListener("DOMContentLoaded", () => {
    loadPurchases();
});

function getToken() {
    return localStorage.getItem("accessToken");
}

async function loadPurchases() {
    const token = getToken();

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return;
    }

    try {
        const response = await fetch(PURCHASE_API.list, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSamplePurchases();
            return;
        }

        allPurchases = normalizePurchases(result.data).map(normalizePurchase);
        renderPurchases();

    } catch (error) {
        console.error(error);

        // 백엔드 API 연결 전 화면 확인용 예시 데이터
        renderSamplePurchases();
    }
}

function normalizePurchases(data) {
    if (!data) {
        return [];
    }

    if (Array.isArray(data)) {
        return data;
    }

    if (Array.isArray(data.content)) {
        return data.content;
    }

    if (Array.isArray(data.purchases)) {
        return data.purchases;
    }

    return [];
}

function normalizePurchase(purchase) {
    const saleType = purchase.saleType || purchase.purchaseType;
    const pinStatus = purchase.pinStatus;
    const totalAmount = purchase.totalAmount ?? purchase.totalPrice ?? 0;

    return {
        ...purchase,
        purchaseType: saleType,
        totalAmount: totalAmount,
        pointAmount: purchase.pointAmount ?? 0,
        cardAmount: purchase.cardAmount ?? totalAmount,
        paymentStatus: purchase.paymentStatus || "COMPLETED",
        pinOpened: purchase.pinOpened ?? pinStatus === "REVEALED",
        imageText: purchase.imageText || purchase.brand || "GIFT CARD",
        bgClass: purchase.bgClass || getPurchaseBgClass(purchase.brand)
    };
}

function renderSamplePurchases() {
    allPurchases = [
        {
            purchaseId: 1,
            saleId: 1,
            productName: "스타벅스 아메리카노 Tall",
            purchaseType: "PERSONAL",
            purchaseStatus: "COMPLETED",
            paymentStatus: "COMPLETED",
            totalAmount: 4300,
            pointAmount: 0,
            cardAmount: 4300,
            purchasedAt: "2026-06-22T14:30:00",
            pinNumber: "1234-5678-9012-3456",
            pinOpened: true,
            imageText: "STARBUCKS",
            bgClass: "bg-starbucks"
        },
        {
            purchaseId: 2,
            saleId: 2,
            productName: "문화상품권 1만원권",
            purchaseType: "PLATFORM",
            purchaseStatus: "PENDING_CONFIRM",
            paymentStatus: "COMPLETED",
            totalAmount: 9200,
            pointAmount: 1000,
            cardAmount: 8200,
            purchasedAt: "2026-06-23T11:20:00",
            pinNumber: "9876-5432-1098-7654",
            pinOpened: false,
            imageText: "CULTURE LAND",
            bgClass: "bg-culture"
        }
    ];

    renderPurchases();
}

function renderPurchases() {
    const purchaseList = document.getElementById("purchaseList");

    const filteredPurchases = allPurchases.filter(purchase => {
        if (currentFilter === "ALL") {
            return true;
        }

        if (currentFilter === "PERSONAL") {
            return purchase.purchaseType === "PERSONAL";
        }

        if (currentFilter === "PLATFORM") {
            return purchase.purchaseType === "PLATFORM";
        }

        if (currentFilter === "PENDING_CONFIRM") {
            return purchase.purchaseStatus === "PENDING_CONFIRM";
        }

        if (currentFilter === "CONFIRMED") {
            return purchase.purchaseStatus === "CONFIRMED" || purchase.pinOpened === true;
        }

        return true;
    });

    if (filteredPurchases.length === 0) {
        purchaseList.innerHTML = `
            <div class="empty-purchase-box">
                <h3>구매내역이 없습니다.</h3>
                <p>원하는 기프트카드를 구매해보세요.</p>
            </div>
        `;
        return;
    }

    purchaseList.innerHTML = filteredPurchases.map(purchase => createPurchaseCard(purchase)).join("");
}

function createPurchaseCard(purchase) {
    const purchaseTypeText = purchase.purchaseType === "PERSONAL" ? "개인 판매" : "관리자 판매";
    const purchaseTypeClass = purchase.purchaseType === "PERSONAL" ? "personal" : "platform";

    const statusText = getPurchaseStatusText(purchase);
    const statusClass = purchase.purchaseStatus === "PENDING_CONFIRM" ? "pending" : "completed";

    const pinText = purchase.pinOpened
        ? `핀 번호 : ${purchase.pinNumber || "확인된 핀번호"}`
        : "핀 번호 : ****-****-****-****";

    return `
        <article class="purchase-history-card">
            <div class="purchase-image ${purchase.bgClass || "bg-money"}">
                ${escapeHtml(purchase.imageText || "GIFT CARD")}
            </div>

            <div class="purchase-info">
                <div class="purchase-badges">
                    <span class="purchase-badge ${purchaseTypeClass}">${purchaseTypeText}</span>
                    <span class="purchase-badge ${statusClass}">${statusText}</span>
                </div>

                <h3>${escapeHtml(purchase.productName || purchase.gifticonName || "상품명")}</h3>

                <p class="purchase-detail-row">
                    <strong>구매일</strong>
                    ${formatDate(purchase.purchasedAt || purchase.createdAt)}
                </p>

                <p class="purchase-detail-row">
                    <strong>결제 상태</strong>
                    ${purchase.paymentStatus || "COMPLETED"}
                </p>

                <p class="purchase-detail-row">
                    <strong>결제 금액</strong>
                    ${formatNumber(purchase.totalAmount || 0)}원
                    <span>
                        (포인트 ${formatNumber(purchase.pointAmount || 0)} + 카드 ${formatNumber(purchase.cardAmount || purchase.totalAmount || 0)})
                    </span>
                </p>

                <div class="purchase-pin-preview">
                    ${escapeHtml(pinText)}
                </div>
            </div>

            <div class="purchase-actions">
                <button class="pin-view-btn" onclick="openPinModal(${purchase.purchaseId})">
                    핀번호 보기
                </button>

                <button class="product-view-btn" onclick="location.href='./product-detail.html?saleId=${purchase.saleId || ""}'">
                    상품 조회
                </button>

                ${canRequestRefund(purchase)
        ? `<button class="refund-btn" onclick="requestRefund(${purchase.purchaseId})">환불 요청</button>`
        : ""
    }
            </div>
        </article>
    `;
}

function changePurchaseFilter(filter, button) {
    currentFilter = filter;

    document.querySelectorAll(".filter-btn").forEach(btn => {
        btn.classList.remove("active");
    });

    button.classList.add("active");
    renderPurchases();
}

async function openPinModal(purchaseId) {
    selectedPurchase = allPurchases.find(purchase => Number(purchase.purchaseId) === Number(purchaseId));

    if (!selectedPurchase) {
        alert("구매내역을 찾을 수 없습니다.");
        return;
    }

    try {
        const token = getToken();
        const response = await fetch(PURCHASE_API.detail(purchaseId), {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json().catch(() => null);

        if (response.ok && result?.success !== false && result?.data) {
            selectedPurchase = normalizePurchase({
                ...selectedPurchase,
                ...result.data
            });

            allPurchases = allPurchases.map(purchase =>
                Number(purchase.purchaseId) === Number(purchaseId) ? selectedPurchase : purchase
            );
        }
    } catch (error) {
        console.error(error);
    }

    document.getElementById("modalProductName").textContent =
        selectedPurchase.productName || selectedPurchase.gifticonName || "상품명";

    const modalPinNumber = document.getElementById("modalPinNumber");
    const pinRevealBtn = document.getElementById("pinRevealBtn");
    const purchaseConfirmBtn = document.getElementById("purchaseConfirmBtn");

    modalPinNumber.textContent = selectedPurchase.pinOpened
        ? selectedPurchase.pinNumber || "확인된 핀번호"
        : "****-****-****-****";

    pinRevealBtn.classList.remove("hidden");
    purchaseConfirmBtn.classList.add("hidden");

    if (selectedPurchase.purchaseType === "PLATFORM" && !selectedPurchase.pinOpened) {
        pinRevealBtn.textContent = "핀번호 확인";
        purchaseConfirmBtn.classList.remove("hidden");
    } else {
        pinRevealBtn.textContent = "핀번호 보기";
    }

    document.getElementById("pinModal").classList.remove("hidden");
}

function closePinModal() {
    document.getElementById("pinModal").classList.add("hidden");
    selectedPurchase = null;
}

async function revealPinNumber() {
    if (!selectedPurchase) {
        return;
    }

    if (selectedPurchase.purchaseType === "PLATFORM" && !selectedPurchase.pinOpened) {
        await confirmPurchase();
        return;
    }

    document.getElementById("modalPinNumber").textContent =
        selectedPurchase.pinNumber || "확인된 핀번호";

    selectedPurchase.pinOpened = true;
    renderPurchases();
}

async function confirmPurchase() {
    if (!selectedPurchase) {
        return;
    }

    if (!confirm("핀번호를 확인 완료 처리하시겠습니까?\n확인 완료 후에는 환불이 제한될 수 있습니다.")) {
        return;
    }

    const token = getToken();

    try {
        const response = await fetch(PURCHASE_API.revealPin(selectedPurchase.purchaseId), {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json().catch(() => null);

        if (!response.ok || result?.success === false) {
            alert(result?.message || "구매 확정 처리에 실패했습니다.");
            return;
        }

        selectedPurchase = normalizePurchase({
            ...selectedPurchase,
            ...result.data
        });

        allPurchases = allPurchases.map(purchase =>
            Number(purchase.purchaseId) === Number(selectedPurchase.purchaseId) ? selectedPurchase : purchase
        );

        document.getElementById("modalPinNumber").textContent =
            selectedPurchase.pinNumber || "확인된 핀번호";

        alert("구매 확정이 완료되었습니다.");
        renderPurchases();

    } catch (error) {
        console.error(error);

        // API 연결 전 화면 확인용 처리
        selectedPurchase.purchaseStatus = "CONFIRMED";
        selectedPurchase.pinOpened = true;

        document.getElementById("modalPinNumber").textContent =
            selectedPurchase.pinNumber || "1234-5678-9012-3456";

        alert("구매 확정 처리되었습니다. API 연결 전 예시 처리입니다.");
        renderPurchases();
    }
}

function requestRefund(purchaseId) {
    const purchase = allPurchases.find(item => Number(item.purchaseId) === Number(purchaseId));

    if (!purchase) {
        alert("구매내역을 찾을 수 없습니다.");
        return;
    }

    if (purchase.purchaseType === "PERSONAL" && purchase.pinOpened) {
        alert("핀번호를 확인한 개인 판매 상품은 환불 요청이 제한될 수 있습니다.");
        return;
    }

    if (purchase.purchaseStatus === "CONFIRMED") {
        alert("구매 확정된 상품은 환불 요청이 제한될 수 있습니다.");
        return;
    }

    selectedRefundPurchase = purchase;

    document.getElementById("refundProductName").textContent =
        purchase.productName || purchase.gifticonName || "상품명";

    document.getElementById("refundTotalAmount").textContent =
        `${formatNumber(purchase.totalAmount || 0)}원`;

    document.getElementById("refundPointAmount").textContent =
        `${formatNumber(purchase.pointAmount || 0)} P`;

    document.getElementById("refundCardAmount").textContent =
        `${formatNumber(purchase.cardAmount || purchase.totalAmount || 0)}원`;

    document.getElementById("refundReason").value = "";
    document.getElementById("refundReasonDetail").value = "";

    document.getElementById("refundModal").classList.remove("hidden");
}

function getPurchaseStatusText(purchase) {
    if (purchase.purchaseStatus === "PENDING_CONFIRM") {
        return "확인 대기";
    }

    if (purchase.purchaseStatus === "CONFIRMED") {
        return "확인 완료";
    }

    if (purchase.purchaseStatus === "REFUND_REQUESTED") {
        return "환불 요청";
    }

    if (purchase.purchaseStatus === "REFUNDED") {
        return "환불 완료";
    }

    if (purchase.pinOpened) {
        return "핀번호 확인";
    }

    return "구매 완료";
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleDateString("ko-KR");
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function getPurchaseBgClass(brand) {
    const normalizedBrand = String(brand || "").toLowerCase();

    if (normalizedBrand.includes("starbucks") || normalizedBrand.includes("스타벅스")) {
        return "bg-starbucks";
    }

    if (normalizedBrand.includes("bhc")) {
        return "bg-bhc";
    }

    if (normalizedBrand.includes("olive") || normalizedBrand.includes("올리브")) {
        return "bg-olive";
    }

    if (normalizedBrand.includes("cu")) {
        return "bg-cu";
    }

    if (normalizedBrand.includes("mega") || normalizedBrand.includes("메가박스")) {
        return "bg-megabox";
    }

    return "bg-money";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function closeRefundModal() {
    document.getElementById("refundModal").classList.add("hidden");
    selectedRefundPurchase = null;
}

async function submitRefundRequest() {
    if (!selectedRefundPurchase) {
        alert("환불 요청할 상품 정보를 찾을 수 없습니다.");
        return;
    }

    const reason = document.getElementById("refundReason").value;
    const reasonDetail = document.getElementById("refundReasonDetail").value.trim();

    if (!reason) {
        alert("환불 사유를 선택해주세요.");
        return;
    }

    if (!reasonDetail) {
        alert("상세 사유를 입력해주세요.");
        return;
    }

    if (!confirm("환불 요청을 진행하시겠습니까?")) {
        return;
    }

    const token = getToken();

    const refundRequest = {
        purchaseId: selectedRefundPurchase.purchaseId,
        paymentId: selectedRefundPurchase.paymentId,
        reason: reason,
        reasonDetail: reasonDetail,
        refundAmount: selectedRefundPurchase.totalAmount || 0,
        pointAmount: selectedRefundPurchase.pointAmount || 0,
        cardAmount: selectedRefundPurchase.cardAmount || selectedRefundPurchase.totalAmount || 0
    };

    try {
        const response = await fetch(PURCHASE_API.refund, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(refundRequest)
        });

        const result = await response.json().catch(() => null);

        if (!response.ok || result?.success === false) {
            alert(result?.message || "환불 요청에 실패했습니다.");
            return;
        }

        alert("환불 요청이 완료되었습니다.");
        closeRefundModal();

        selectedRefundPurchase.purchaseStatus = "REFUND_REQUESTED";
        renderPurchases();

    } catch (error) {
        console.error(error);

        // 백엔드 API 연결 전 화면 확인용 처리
        alert("환불 요청이 완료되었습니다. API 연결 전 예시 처리입니다.");
        closeRefundModal();

        selectedRefundPurchase.purchaseStatus = "REFUND_REQUESTED";
        renderPurchases();
    }
}

function canRequestRefund(purchase) {
    if (purchase.purchaseStatus === "REFUND_REQUESTED") {
        return false;
    }

    if (purchase.purchaseStatus === "REFUNDED") {
        return false;
    }

    if (purchase.purchaseStatus === "CONFIRMED") {
        return false;
    }

    if (purchase.pinOpened === true) {
        return false;
    }

    return purchase.paymentStatus === "COMPLETED";
}

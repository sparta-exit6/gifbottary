let currentPurchaseId = null;

document.addEventListener("DOMContentLoaded", () => {
    loadPurchaseDetail();
});

function getPurchaseDetailToken() {
    return localStorage.getItem("accessToken");
}

async function loadPurchaseDetail() {
    const token = getPurchaseDetailToken();

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return;
    }

    const params = new URLSearchParams(location.search);
    currentPurchaseId = params.get("purchaseId");

    if (!currentPurchaseId) {
        alert("구매 정보를 찾을 수 없습니다.");
        location.href = "./purchases.html";
        return;
    }

    try {
        const response = await fetch(`/api/v1/purchases/${currentPurchaseId}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "구매 상세 조회에 실패했습니다.");
            location.href = "./purchases.html";
            return;
        }

        renderPurchaseDetail(result.data);
    } catch (error) {
        console.error(error);
        alert("구매 상세 조회 중 오류가 발생했습니다.");
    }
}

function renderPurchaseDetail(purchase) {
    const brand = purchase.brand || "GIFT CARD";
    const bgClass = getPurchaseDetailBgClass(brand);
    const image = document.getElementById("purchaseDetailImage");

    image.className = `detail-image ${bgClass}`;
    image.textContent = brand;

    document.getElementById("purchaseSaleType").textContent =
        purchase.saleType === "PLATFORM" ? "관리자 판매" : "개인 판매";
    document.getElementById("purchaseProductName").textContent = purchase.productName || "상품명";
    document.getElementById("purchaseBrand").textContent = `브랜드 ${brand}`;
    document.getElementById("purchaseTotalPrice").textContent =
        `결제 금액 ${formatPurchaseDetailNumber(purchase.totalPrice)}원`;
    document.getElementById("purchaseStatus").textContent = getPurchaseStatusLabel(purchase.purchaseStatus);
    document.getElementById("purchasePinStatus").textContent = getPinStatusLabel(purchase.pinStatus);
    document.getElementById("purchaseQuantity").textContent = `${purchase.quantity || 0}개`;
    document.getElementById("purchaseExpireAt").textContent = purchase.expireAt || "-";
    document.getElementById("purchasePurchasedAt").textContent = formatPurchaseDetailDate(purchase.purchasedAt);
    document.getElementById("purchaseConfirmedAt").textContent = formatPurchaseDetailDate(purchase.confirmedAt);
    document.getElementById("purchasePinNumber").textContent =
        purchase.pinNumber || "****-****-****-****";

    const revealButton = document.getElementById("revealPinButton");

    if (purchase.saleType === "PLATFORM" && purchase.pinStatus === "MASKED") {
        revealButton.classList.remove("hidden");
    } else {
        revealButton.classList.add("hidden");
    }
}

async function revealPurchasePin() {
    if (!currentPurchaseId) {
        return;
    }

    if (!confirm("핀번호를 확인하면 환불이 제한될 수 있습니다. 계속하시겠습니까?")) {
        return;
    }

    const token = getPurchaseDetailToken();

    try {
        const response = await fetch(`/api/v1/purchases/${currentPurchaseId}/reveal-pin`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "핀번호 확인에 실패했습니다.");
            return;
        }

        renderPurchaseDetail(result.data);
        alert("핀번호가 공개되었습니다.");
    } catch (error) {
        console.error(error);
        alert("핀번호 확인 중 오류가 발생했습니다.");
    }
}

function getPurchaseStatusLabel(status) {
    switch (status) {
        case "PENDING_PAYMENT":
            return "결제 대기";
        case "PAID":
            return "결제 완료";
        case "CONFIRMED":
            return "구매 확정";
        case "REFUNDED":
            return "환불 완료";
        default:
            return status || "-";
    }
}

function getPinStatusLabel(status) {
    switch (status) {
        case "MASKED":
            return "마스킹";
        case "REVEALED":
            return "공개";
        default:
            return status || "-";
    }
}

function formatPurchaseDetailNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function formatPurchaseDetailDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("ko-KR");
}

function getPurchaseDetailBgClass(brand) {
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

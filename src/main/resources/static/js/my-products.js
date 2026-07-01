let allMyProducts = [];
let currentMyProductFilter = "ALL";
let selectedSaleId = null;

const MY_PRODUCT_API = {
    // 팀 API가 다르면 여기만 수정하면 됩니다.
    list: "/api/v1/products/me",
    delete: (saleId) => `/api/v1/products/${saleId}`,
    changeStatus: (saleId) => `/api/v1/products/${saleId}/status`
};

document.addEventListener("DOMContentLoaded", () => {
    loadMyProducts();
});

function getAccessTokenForMyProducts() {
    return localStorage.getItem("accessToken");
}

async function loadMyProducts() {
    const token = getAccessTokenForMyProducts();

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return;
    }

    try {
        const response = await fetch(MY_PRODUCT_API.list, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSampleMyProducts();
            return;
        }

        allMyProducts = normalizeMyProducts(result.data).map(normalizeMyProduct);
        renderMyProducts();
        renderMyProductSummary();

    } catch (error) {
        console.error(error);

        // 백엔드 API 연결 전 화면 확인용 예시 데이터
        renderSampleMyProducts();
    }
}

function normalizeMyProducts(data) {
    if (!data) {
        return [];
    }

    if (Array.isArray(data)) {
        return data;
    }

    if (Array.isArray(data.content)) {
        return data.content;
    }

    if (Array.isArray(data.products)) {
        return data.products;
    }

    if (Array.isArray(data.sales)) {
        return data.sales;
    }

    return [];
}

function normalizeMyProduct(product) {
    return {
        ...product,
        originalPrice: product.originalPrice ?? product.faceValue ?? product.price,
        imageText: product.imageText || product.brand || "GIFT CARD",
        bgClass: product.bgClass || getMyProductBgClass(product.brand)
    };
}

function renderSampleMyProducts() {
    allMyProducts = [
        {
            saleId: 1,
            productId: 101,
            productName: "스타벅스 아메리카노 Tall",
            originalPrice: 5000,
            salePrice: 4300,
            saleStatus: "SELLING",
            expireAt: "2026-07-30",
            createdAt: "2026-06-22T14:30:00",
            pinNumber: "****-****-****-****",
            imageText: "STARBUCKS",
            bgClass: "bg-starbucks"
        },
        {
            saleId: 2,
            productId: 102,
            productName: "CU 5천원권",
            originalPrice: 5000,
            salePrice: 4400,
            saleStatus: "SOLD_OUT",
            expireAt: "2026-08-15",
            createdAt: "2026-06-24T10:20:00",
            pinNumber: "****-****-****-****",
            imageText: "CU",
            bgClass: "bg-cu"
        },
        {
            saleId: 3,
            productId: 103,
            productName: "올리브영 1만원권",
            originalPrice: 10000,
            salePrice: 9000,
            saleStatus: "EXPIRED",
            expireAt: "2026-06-20",
            createdAt: "2026-06-01T09:10:00",
            pinNumber: "****-****-****-****",
            imageText: "OLIVE YOUNG",
            bgClass: "bg-olive"
        }
    ];

    renderMyProducts();
    renderMyProductSummary();
}

function renderMyProductSummary() {
    const totalCount = allMyProducts.length;
    const sellingCount = allMyProducts.filter(product =>
        product.saleStatus === "SELLING" || product.saleStatus === "ON_SALE"
    ).length;
    const soldOutCount = allMyProducts.filter(product => product.saleStatus === "SOLD_OUT").length;

    document.getElementById("totalCount").textContent = totalCount;
    document.getElementById("sellingCount").textContent = sellingCount;
    document.getElementById("soldOutCount").textContent = soldOutCount;
}

function renderMyProducts() {
    const myProductList = document.getElementById("myProductList");

    const filteredProducts = allMyProducts.filter(product => {
        if (currentMyProductFilter === "ALL") {
            return true;
        }

        return product.saleStatus === currentMyProductFilter;
    });

    if (filteredProducts.length === 0) {
        myProductList.innerHTML = `
            <div class="empty-my-product-box">
                <h3>판매내역이 없습니다.</h3>
                <p>기프트카드를 등록하고 판매를 시작해보세요.</p>
                <button onclick="goRegisterPage()">상품 등록하기</button>
            </div>
        `;
        return;
    }

    myProductList.innerHTML = filteredProducts.map(product => createMyProductCard(product)).join("");
}

function createMyProductCard(product) {
    const statusText = getSaleStatusText(product.saleStatus);
    const statusClass = getSaleStatusClass(product.saleStatus);

    return `
        <article class="my-product-card">
            <div class="my-product-image ${product.bgClass || "bg-money"}">
                ${escapeHtmlForMyProducts(product.imageText || "GIFT CARD")}
            </div>

            <div class="my-product-info">
                <div class="my-product-badges">
                    <span class="my-product-badge personal">개인 판매</span>
                    <span class="my-product-badge ${statusClass}">${statusText}</span>
                </div>

                <h3>${escapeHtmlForMyProducts(product.productName || product.name || "상품명")}</h3>

                <p class="my-product-detail-row">
                    <strong>정가</strong>
                    ${formatNumberForMyProducts(product.originalPrice || product.price || 0)}원
                </p>

                <p class="my-product-detail-row">
                    <strong>판매가</strong>
                    ${formatNumberForMyProducts(product.salePrice || 0)}원
                </p>

                <p class="my-product-detail-row">
                    <strong>유효기간</strong>
                    ${formatDateForMyProducts(product.expireAt || product.expiredAt)}
                </p>

                <p class="my-product-detail-row">
                    <strong>등록일</strong>
                    ${formatDateForMyProducts(product.createdAt)}
                </p>

                <div class="my-product-pin-box">
                    핀 번호 : ${product.pinNumber || "****-****-****-****"}
                </div>
            </div>

            <div class="my-product-actions">
                <button class="view-product-btn" onclick="location.href='./product-detail.html?saleId=${product.saleId}'">
                    상품 보기
                </button>

                <button class="edit-product-btn" onclick="location.href='./product-edit.html?saleId=${product.saleId}'">
                    수정하기
                </button>

                <button class="status-product-btn" onclick="openStatusModal(${product.saleId})">
                    상태 변경
                </button>

            </div>
        </article>
    `;
}

function changeMyProductFilter(filter, button) {
    currentMyProductFilter = filter;

    document.querySelectorAll(".filter-btn").forEach(btn => {
        btn.classList.remove("active");
    });

    button.classList.add("active");
    renderMyProducts();
}

function openStatusModal(saleId) {
    const product = allMyProducts.find(item => Number(item.saleId) === Number(saleId));

    if (!product) {
        alert("상품 정보를 찾을 수 없습니다.");
        return;
    }

    selectedSaleId = saleId;
    document.getElementById("statusModalProductName").textContent =
        product.productName || product.name || "상품명";

    document.getElementById("statusModal").classList.remove("hidden");
}

function closeStatusModal() {
    selectedSaleId = null;
    document.getElementById("statusModal").classList.add("hidden");
}

async function changeSaleStatus(status) {
    if (!selectedSaleId) {
        alert("상품 정보를 찾을 수 없습니다.");
        return;
    }

    const token = getAccessTokenForMyProducts();

    try {
        const response = await fetch(MY_PRODUCT_API.changeStatus(selectedSaleId), {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify({
                status: status
            })
        });

        const result = await response.json().catch(() => null);

        if (!response.ok || result?.success === false) {
            alert(result?.message || "판매 상태 변경에 실패했습니다.");
            return;
        }

        updateLocalStatus(selectedSaleId, status);
        alert("판매 상태가 변경되었습니다.");
        closeStatusModal();

    } catch (error) {
        console.error(error);

        // API 연결 전 화면 확인용 처리
        updateLocalStatus(selectedSaleId, status);
        alert("판매 상태가 변경되었습니다. API 연결 전 예시 처리입니다.");
        closeStatusModal();
    }
}

function updateLocalStatus(saleId, status) {
    allMyProducts = allMyProducts.map(product => {
        if (Number(product.saleId) === Number(saleId)) {
            return {
                ...product,
                saleStatus: status
            };
        }

        return product;
    });

    renderMyProducts();
    renderMyProductSummary();
}

async function deleteMyProduct(saleId) {
    if (!confirm("정말 상품을 삭제하시겠습니까?")) {
        return;
    }

    const token = getAccessTokenForMyProducts();

    try {
        const response = await fetch(MY_PRODUCT_API.delete(saleId), {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json().catch(() => null);

        if (!response.ok || result?.success === false) {
            alert(result?.message || "상품 삭제에 실패했습니다.");
            return;
        }

        removeLocalProduct(saleId);
        alert("상품이 삭제되었습니다.");

    } catch (error) {
        console.error(error);

        // API 연결 전 화면 확인용 처리
        removeLocalProduct(saleId);
        alert("상품이 삭제되었습니다. API 연결 전 예시 처리입니다.");
    }
}

function removeLocalProduct(saleId) {
    allMyProducts = allMyProducts.filter(product => Number(product.saleId) !== Number(saleId));

    renderMyProducts();
    renderMyProductSummary();
}

function getSaleStatusText(status) {
    switch (status) {
        case "SELLING":
        case "ON_SALE":
            return "판매중";
        case "PENDING_REVIEW":
            return "검수전";
        case "SOLD_OUT":
            return "판매완료";
        case "CANCELLED":
            return "판매취소";
        case "PIN_INVALID":
            return "검수반려";
        case "EXPIRED":
            return "기간만료";
        default:
            return "검수전";
    }
}

function getSaleStatusClass(status) {
    switch (status) {
        case "SELLING":
        case "ON_SALE":
            return "selling";
        case "PENDING_REVIEW":
            return "pending";
        case "SOLD_OUT":
            return "sold-out";
        case "CANCELLED":
        case "PIN_INVALID":
            return "expired";
        case "EXPIRED":
            return "expired";
        default:
            return "pending";
    }
}

function getMyProductBgClass(brand) {
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

function formatDateForMyProducts(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleDateString("ko-KR");
}

function formatNumberForMyProducts(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function escapeHtmlForMyProducts(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

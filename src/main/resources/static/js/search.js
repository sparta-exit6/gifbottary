let searchKeyword = "";
let searchType = "ALL";

const SEARCH_API = {
    products: "/api/v1/search/products",
    popularKeywords: "/api/v1/search/popular-keywords"
};

document.addEventListener("DOMContentLoaded", () => {
    initSearchPage();
});

function initSearchPage() {
    const params = new URLSearchParams(location.search);
    searchKeyword = params.get("keyword") || "";

    document.getElementById("searchKeywordInput").value = searchKeyword;
    document.getElementById("searchKeywordText").textContent = searchKeyword || "전체";

    loadPopularKeywords();
    loadSearchProducts();
}

function handleSearchEnter(event) {
    if (event.key === "Enter") {
        moveSearchPage();
    }
}

function moveSearchPage() {
    const keyword = document.getElementById("searchKeywordInput").value.trim();

    if (!keyword) {
        alert("검색어를 입력해주세요.");
        return;
    }

    location.href = `./search.html?keyword=${encodeURIComponent(keyword)}`;
}

async function loadPopularKeywords() {
    try {
        const response = await fetch(SEARCH_API.popularKeywords);
        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSamplePopularKeywords();
            return;
        }

        const keywords = normalizePopularKeywords(result.data);
        renderPopularKeywords(keywords);

    } catch (error) {
        console.error(error);
        renderSamplePopularKeywords();
    }
}

function normalizePopularKeywords(data) {
    if (!data) {
        return [];
    }

    if (Array.isArray(data)) {
        return data;
    }

    if (Array.isArray(data.keywords)) {
        return data.keywords;
    }

    if (Array.isArray(data.content)) {
        return data.content;
    }

    return [];
}

function renderSamplePopularKeywords() {
    renderPopularKeywords([
        "스타벅스",
        "메가박스",
        "올리브영",
        "CU",
        "문화상품권",
        "구글 기프트카드"
    ]);
}

function renderPopularKeywords(keywords) {
    const target = document.getElementById("popularKeywordList");

    if (!keywords.length) {
        target.innerHTML = `<span class="popular-keyword">인기 검색어 없음</span>`;
        return;
    }

    target.innerHTML = keywords.map(item => {
        const keyword = typeof item === "string"
            ? item
            : item.keyword || item.name || "";

        return `
            <button class="popular-keyword" onclick="searchByKeyword('${escapeQuote(keyword)}')">
                #${escapeHtml(keyword)}
            </button>
        `;
    }).join("");
}

function searchByKeyword(keyword) {
    location.href = `./search.html?keyword=${encodeURIComponent(keyword)}`;
}

async function loadSearchProducts() {
    const sort = document.getElementById("searchSort").value;

    const queryParams = new URLSearchParams();
    queryParams.append("keyword", searchKeyword);
    queryParams.append("sort", sort);

    if (searchType !== "ALL") {
        queryParams.append("type", searchType);
    }

    try {
        const response = await fetch(`${SEARCH_API.products}?${queryParams.toString()}`);
        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSampleSearchProducts();
            return;
        }

        const products = normalizeSearchProducts(result.data);
        renderSearchProducts(products);

    } catch (error) {
        console.error(error);

        // 백엔드 API 연결 전 화면 확인용 예시 데이터
        renderSampleSearchProducts();
    }
}

function normalizeSearchProducts(data) {
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

function renderSampleSearchProducts() {
    const sampleProducts = [
        {
            saleId: 1,
            productName: "스타벅스 아메리카노 Tall",
            productType: "PERSONAL",
            originalPrice: 5000,
            salePrice: 4300,
            sellerName: "김민혁",
            status: "판매중",
            imageText: "STARBUCKS",
            bgClass: "bg-starbucks"
        },
        {
            saleId: 2,
            productName: "스타벅스 e카드 5천원권",
            productType: "PLATFORM",
            originalPrice: 5000,
            salePrice: 4700,
            sellerName: "기프보따리",
            status: "판매중",
            imageText: "STARBUCKS",
            bgClass: "bg-starbucks"
        },
        {
            saleId: 3,
            productName: "올리브영 1만원권",
            productType: "PERSONAL",
            originalPrice: 10000,
            salePrice: 9000,
            sellerName: "이지현",
            status: "판매중",
            imageText: "OLIVE YOUNG",
            bgClass: "bg-olive"
        },
        {
            saleId: 4,
            productName: "문화상품권 1만원권",
            productType: "PLATFORM",
            originalPrice: 10000,
            salePrice: 9200,
            sellerName: "기프보따리",
            status: "판매중",
            imageText: "CULTURE LAND",
            bgClass: "bg-culture"
        },
        {
            saleId: 5,
            productName: "CU 5천원권",
            productType: "PERSONAL",
            originalPrice: 5000,
            salePrice: 4400,
            sellerName: "박서준",
            status: "판매중",
            imageText: "CU",
            bgClass: "bg-cu"
        }
    ];

    const filtered = sampleProducts.filter(product => {
        const matchesKeyword = !searchKeyword ||
            product.productName.includes(searchKeyword) ||
            product.imageText.includes(searchKeyword);

        const matchesType = searchType === "ALL" || product.productType === searchType;

        return matchesKeyword && matchesType;
    });

    renderSearchProducts(filtered);
}

function renderSearchProducts(products) {
    const target = document.getElementById("searchProductList");
    const count = document.getElementById("searchResultCount");

    count.textContent = `총 ${products.length}개의 상품`;

    if (!products.length) {
        target.innerHTML = `
            <div class="empty-search-box">
                <h3>검색 결과가 없습니다.</h3>
                <p>다른 검색어로 다시 검색해보세요.</p>
            </div>
        `;
        return;
    }

    target.innerHTML = products.map(product => createSearchProductCard(product)).join("");
}

function createSearchProductCard(product) {
    const type = product.productType || product.type || "PERSONAL";
    const typeText = type === "PLATFORM" ? "관리자 판매" : "개인 판매";
    const typeClass = type === "PLATFORM" ? "platform" : "personal";

    return `
        <article class="search-product-card" onclick="location.href='./product-detail.html?saleId=${product.saleId || product.id}'">
            <div class="search-product-image ${product.bgClass || "bg-money"}">
                ${escapeHtml(product.imageText || "GIFT CARD")}
            </div>

            <span class="search-product-type ${typeClass}">
                ${typeText}
            </span>

            <h3>${escapeHtml(product.productName || product.name || "상품명")}</h3>

            <p class="search-origin-price">
                정가 ${formatNumber(product.originalPrice || product.price || 0)}원
            </p>

            <p class="search-sale-price">
                판매가 ${formatNumber(product.salePrice || 0)}원
            </p>

            <div class="search-product-meta">
                <span>${escapeHtml(product.sellerName || "판매자")}</span>
                <span>${escapeHtml(product.status || "판매중")}</span>
            </div>
        </article>
    `;
}

function changeSearchType(type, button) {
    searchType = type;

    document.querySelectorAll(".filter-btn").forEach(btn => {
        btn.classList.remove("active");
    });

    button.classList.add("active");

    loadSearchProducts();
}

function reloadSearchProducts() {
    loadSearchProducts();
}

function formatNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeQuote(value) {
    return String(value)
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'");
}
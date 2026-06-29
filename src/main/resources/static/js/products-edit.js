let editSaleId = null;
let originalSaleStatus = null;

const PRODUCT_EDIT_API = {
    detail: (saleId) => `/api/v1/products/${saleId}`,
    update: (saleId) => `/api/v1/products/${saleId}`,
    changeStatus: (saleId) => `/api/v1/products/${saleId}/status`,
    delete: (saleId) => `/api/v1/products/${saleId}`
};

document.addEventListener("DOMContentLoaded", () => {
    initProductEditPage();
});

function getEditToken() {
    return localStorage.getItem("accessToken");
}

function initProductEditPage() {
    const token = getEditToken();

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return;
    }

    const params = new URLSearchParams(location.search);
    editSaleId = params.get("saleId");

    if (!editSaleId) {
        alert("수정할 상품 정보를 찾을 수 없습니다.");
        location.href = "./my-products.html";
        return;
    }

    loadProductForEdit(editSaleId);
}

async function loadProductForEdit(saleId) {
    const token = getEditToken();

    try {
        const response = await fetch(PRODUCT_EDIT_API.detail(saleId), {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            renderSampleEditProduct();
            return;
        }

        renderEditProduct(result.data);

    } catch (error) {
        console.error(error);

        // 백엔드 API 연결 전 화면 확인용 예시 데이터
        renderSampleEditProduct();
    }
}

function renderSampleEditProduct() {
    renderEditProduct({
        saleId: editSaleId,
        productName: "스타벅스 아메리카노 Tall",
        originalPrice: 5000,
        salePrice: 4300,
        expiredAt: "2026-07-30",
        saleStatus: "SELLING",
        description: "스타벅스 아메리카노 Tall 기프티콘입니다.",
        imageUrl: null,
        imageText: "STARBUCKS"
    });
}

function renderEditProduct(product) {
    document.getElementById("editProductName").value =
        product.productName || product.name || product.gifticonName || "";

    document.getElementById("editOriginalPrice").value =
        product.originalPrice || product.price || "";

    document.getElementById("editSalePrice").value =
        product.salePrice || "";

    document.getElementById("editExpiredAt").value =
        formatDateInput(product.expiredAt || product.expireAt || product.expirationDate);

    document.getElementById("editSaleStatus").value =
        product.saleStatus || product.status || "SELLING";

    document.getElementById("editDescription").value =
        product.description || "";

    originalSaleStatus = product.saleStatus || product.status || "SELLING";

    const preview = document.getElementById("editImagePreview");

    if (product.imageUrl) {
        preview.innerHTML = `<img src="${product.imageUrl}" alt="상품 이미지">`;
    } else {
        preview.textContent = product.imageText || "🎁";
    }
}

function previewEditImage(event) {
    const file = event.target.files[0];
    const preview = document.getElementById("editImagePreview");

    if (!file) {
        return;
    }

    const reader = new FileReader();

    reader.onload = function (e) {
        preview.innerHTML = `<img src="${e.target.result}" alt="상품 이미지">`;
    };

    reader.readAsDataURL(file);
}

async function submitProductEdit() {
    const token = getEditToken();

    const productName = document.getElementById("editProductName").value.trim();
    const originalPrice = document.getElementById("editOriginalPrice").value.trim();
    const salePrice = document.getElementById("editSalePrice").value.trim();
    const expiredAt = document.getElementById("editExpiredAt").value;
    const saleStatus = document.getElementById("editSaleStatus").value;
    const description = document.getElementById("editDescription").value.trim();
    const image = document.getElementById("editProductImage").files[0];

    if (!productName) {
        alert("기프트카드 이름을 입력해주세요.");
        return;
    }

    if (!originalPrice) {
        alert("정가를 입력해주세요.");
        return;
    }

    if (!salePrice) {
        alert("판매가를 입력해주세요.");
        return;
    }

    if (Number(salePrice) > Number(originalPrice)) {
        alert("판매가는 정가보다 높을 수 없습니다.");
        return;
    }

    if (!expiredAt) {
        alert("유효기간을 선택해주세요.");
        return;
    }

    if (!description) {
        alert("상세 정보를 입력해주세요.");
        return;
    }

    const formData = new FormData();
    formData.append("productName", productName);
    formData.append("originalPrice", originalPrice);
    formData.append("salePrice", salePrice);
    formData.append("expiredAt", expiredAt);
    formData.append("description", description);

    if (image) {
        formData.append("image", image);
    }

    try {
        const response = await fetch(PRODUCT_EDIT_API.update(editSaleId), {
            method: "PATCH",
            headers: {
                "Authorization": `Bearer ${token}`
            },
            body: formData
        });

        const result = await response.json().catch(() => null);

        if (!response.ok || result?.success === false) {
            alert(result?.message || "상품 수정에 실패했습니다.");
            return;
        }

        if (saleStatus !== originalSaleStatus) {
            await updateProductStatus(saleStatus);
        }

        alert("상품이 수정되었습니다.");
        location.href = "./my-products.html";

    } catch (error) {
        console.error(error);

        // API 연결 전 화면 확인용 처리
        alert("상품이 수정되었습니다. API 연결 전 예시 처리입니다.");
        location.href = "./my-products.html";
    }
}

async function updateProductStatus(status) {
    const token = getEditToken();

    const response = await fetch(PRODUCT_EDIT_API.changeStatus(editSaleId), {
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
        throw new Error(result?.message || "판매 상태 변경 실패");
    }
}

async function deleteProductFromEditPage() {
    if (!confirm("정말 이 상품을 삭제하시겠습니까?")) {
        return;
    }

    const token = getEditToken();

    try {
        const response = await fetch(PRODUCT_EDIT_API.delete(editSaleId), {
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

        alert("상품이 삭제되었습니다.");
        location.href = "./my-products.html";

    } catch (error) {
        console.error(error);

        // API 연결 전 화면 확인용 처리
        alert("상품이 삭제되었습니다. API 연결 전 예시 처리입니다.");
        location.href = "./my-products.html";
    }
}

function formatDateInput(value) {
    if (!value) {
        return "";
    }

    if (typeof value === "string" && value.length >= 10) {
        return value.substring(0, 10);
    }

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return "";
    }

    return date.toISOString().substring(0, 10);
}
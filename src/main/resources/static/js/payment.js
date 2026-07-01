let selectedPaymentProduct = null;
let userPointBalance = 0;

const PAYMENT_API = {
    myInfo: "/api/v1/auth/me",
    createPayment: "/api/v1/payments",
    portOneConfig: "/api/v1/payments/portone",
    confirmPayment: "/api/v1/payments/confirm"
};

function getPaymentToken() {
    return localStorage.getItem("accessToken");
}

async function openPaymentModal(product) {
    const token = getPaymentToken();

    if (!token) {
        alert("구매는 로그인 후 이용할 수 있습니다.");
        location.href = "./login.html";
        return;
    }

    selectedPaymentProduct = product;

    renderPaymentProduct(product);
    await loadUserPointBalance();
    calculatePaymentAmount();

    document.getElementById("paymentAgree").checked = false;
    document.getElementById("paymentModal").classList.remove("hidden");
}

function closePaymentModal() {
    document.getElementById("paymentModal").classList.add("hidden");
    selectedPaymentProduct = null;
}

function renderPaymentProduct(product) {
    const productImage = document.getElementById("paymentProductImage");
    productImage.className = `payment-product-image ${product.bgClass || "bg-money"}`;
    productImage.textContent = product.imageText || "GIFT CARD";

    document.getElementById("paymentProductName").textContent = product.productName || "상품명";
    document.getElementById("paymentOriginalPrice").textContent =
        `정가 ${formatPaymentNumber(product.originalPrice || 0)}원`;
    document.getElementById("paymentSalePrice").textContent =
        `판매가 ${formatPaymentNumber(product.salePrice || 0)}원`;

    const typeBadge = document.getElementById("paymentProductType");

    if (product.productType === "PLATFORM") {
        typeBadge.textContent = "관리자 판매";
        typeBadge.className = "payment-type-badge platform";
    } else {
        typeBadge.textContent = "개인 판매";
        typeBadge.className = "payment-type-badge personal";
    }

    document.getElementById("modalProductAmount").textContent =
        `${formatPaymentNumber(product.salePrice || 0)}원`;

    document.getElementById("usePointInput").value = 0;
}

async function loadUserPointBalance() {
    const token = getPaymentToken();

    try {
        const response = await fetch(PAYMENT_API.myInfo, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            userPointBalance = 0;
        } else {
            userPointBalance = result.data?.pointBalance || 0;
        }

    } catch (error) {
        console.error(error);

        // API 연결 전 화면 확인용 예시 포인트
        userPointBalance = 12000;
    }

    document.getElementById("modalPointBalance").textContent =
        `${formatPaymentNumber(userPointBalance)} P`;
}

function calculatePaymentAmount() {
    if (!selectedPaymentProduct) {
        return;
    }

    const salePrice = Number(selectedPaymentProduct.salePrice || 0);
    const pointInput = document.getElementById("usePointInput");

    let usePoint = Number(pointInput.value || 0);

    if (usePoint < 0) {
        usePoint = 0;
    }

    if (usePoint > userPointBalance) {
        usePoint = userPointBalance;
    }

    if (usePoint > salePrice) {
        usePoint = salePrice;
    }

    pointInput.value = usePoint;

    const cardAmount = salePrice - usePoint;

    document.getElementById("modalCardAmount").textContent =
        `${formatPaymentNumber(cardAmount)}원`;

    document.getElementById("modalTotalAmount").textContent =
        `${formatPaymentNumber(salePrice)}원`;
}

function useAllPoint() {
    if (!selectedPaymentProduct) {
        return;
    }

    const salePrice = Number(selectedPaymentProduct.salePrice || 0);
    const maxPoint = Math.min(userPointBalance, salePrice);

    document.getElementById("usePointInput").value = maxPoint;
    calculatePaymentAmount();
}

async function requestPayment() {
    const token = getPaymentToken();

    if (!selectedPaymentProduct) {
        alert("결제할 상품 정보가 없습니다.");
        return;
    }

    if (!document.getElementById("paymentAgree").checked) {
        alert("결제 동의가 필요합니다.");
        return;
    }

    const salePrice = Number(selectedPaymentProduct.salePrice || 0);
    const pointAmount = Number(document.getElementById("usePointInput").value || 0);
    const cardAmount = salePrice - pointAmount;

    if (pointAmount > userPointBalance) {
        alert("보유 포인트보다 많이 사용할 수 없습니다.");
        return;
    }

    const paymentRequest = {
        saleId: selectedPaymentProduct.saleId,
        quantity: 1,
        productName: selectedPaymentProduct.productName,
        totalAmount: salePrice,
        pointAmount: pointAmount,
        cardAmount: cardAmount,
        paymentMethod: cardAmount > 0 ? "CARD_AND_POINT" : "POINT"
    };

    try {
        const createResponse = await fetch(PAYMENT_API.createPayment, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(paymentRequest)
        });

        const createResult = await createResponse.json();

        if (!createResponse.ok || createResult.success === false) {
            alert(createResult.message || "결제 생성에 실패했습니다.");
            return;
        }

        const paymentData = createResult.data;
        const paymentId = paymentData?.portOnePaymentId || paymentData?.portonePaymentId;

        if (!paymentId) {
            console.error("Payment create response does not contain portOnePaymentId.", createResult);
            alert("결제 ID를 찾을 수 없습니다.");
            return;
        }

        const configResponse = await fetch(PAYMENT_API.portOneConfig);
        const configResult = await configResponse.json();

        if (!configResponse.ok || configResult.success === false) {
            alert(configResult.message || "PortOne 설정 조회에 실패했습니다.");
            return;
        }

        if (typeof PortOne === "undefined") {
            alert("PortOne 결제 모듈을 불러오지 못했습니다.");
            return;
        }

        const portOneResponse = await PortOne.requestPayment({
            storeId: configResult.data.storeId,
            channelKey: configResult.data.channelKey,
            paymentId: paymentId,
            orderName: selectedPaymentProduct.productName,
            totalAmount: cardAmount,
            currency: "CURRENCY_KRW",
            payMethod: "CARD",
            customer: {
                email: "test@example.com",
                fullName: "테스트 구매자",
                phoneNumber: "01012345678"
            }
        });

        if (portOneResponse.code) {
            alert("결제 실패: " + portOneResponse.message);
            return;
        }

        await confirmPayment(paymentId);

    } catch (error) {
        console.error(error);
        alert("결제 처리 중 오류가 발생했습니다. 콘솔을 확인해주세요.");
    }
}

async function confirmPayment(portOnePaymentId) {
    const token = getPaymentToken();

    const response = await fetch(PAYMENT_API.confirmPayment, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({
            portOnePaymentId: portOnePaymentId
        })
    });

    const result = await response.json().catch(() => null);

    if (!response.ok || result?.success === false) {
        console.error("Payment confirm failed.", result);
        alert(result?.message || "결제 승인에 실패했습니다.");
        return;
    }

    alert("결제가 완료되었습니다.");
    closePaymentModal();
    location.href = "./purchases.html";
}

function formatPaymentNumber(value) {
    return Number(value || 0).toLocaleString("ko-KR");
}

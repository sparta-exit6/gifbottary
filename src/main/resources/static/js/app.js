const personalProducts = [
    {
        name: "스타벅스 아메리카노 Tall",
        originPrice: "정가 5,000원",
        salePrice: "판매가 4,300원",
        imageText: "STARBUCKS",
        bgClass: "bg-starbucks",
        icon: "♡"
    },
    {
        name: "BHC 뿌링클+콜라 1.25L",
        originPrice: "정가 23,000원",
        salePrice: "판매가 20,000원",
        imageText: "BHC",
        bgClass: "bg-bhc",
        icon: "♡"
    },
    {
        name: "올리브영 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,000원",
        imageText: "OLIVE YOUNG",
        bgClass: "bg-olive",
        icon: "♡"
    },
    {
        name: "CU 5천원권",
        originPrice: "정가 5,000원",
        salePrice: "판매가 4,400원",
        imageText: "CU",
        bgClass: "bg-cu",
        icon: "♡"
    },
    {
        name: "메가박스 2D 영화 관람권",
        originPrice: "정가 15,000원",
        salePrice: "판매가 12,500원",
        imageText: "MEGABOX",
        bgClass: "bg-megabox",
        icon: "♡"
    },
    {
        name: "신세계상품권 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,300원",
        imageText: "10000",
        bgClass: "bg-money",
        icon: "♡"
    }
];

const adminProducts = [
    {
        name: "스타벅스 e카드 5천원권",
        originPrice: "정가 5,000원",
        salePrice: "판매가 4,700원",
        imageText: "STARBUCKS",
        bgClass: "bg-starbucks",
        icon: "🛒"
    },
    {
        name: "신세계상품권 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,500원",
        imageText: "10000",
        bgClass: "bg-money",
        icon: "🛒"
    },
    {
        name: "롯데상품권 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,600원",
        imageText: "LOTTE",
        bgClass: "bg-lotte",
        icon: "🛒"
    },
    {
        name: "해피머니 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,200원",
        imageText: "Happy money",
        bgClass: "bg-happy",
        icon: "🛒"
    },
    {
        name: "구글 기프트카드 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,400원",
        imageText: "Google Play",
        bgClass: "bg-google",
        icon: "🛒"
    },
    {
        name: "문화상품권 1만원권",
        originPrice: "정가 10,000원",
        salePrice: "판매가 9,200원",
        imageText: "CULTURE LAND",
        bgClass: "bg-culture",
        icon: "🛒"
    }
];

function renderProducts(targetId, products) {
    const target = document.getElementById(targetId);

    if (!target) {
        return;
    }

    target.innerHTML = products.map(product => `
        <article class="product-card" onclick="location.href='./product-detail.html?saleId=${product.id || 1}'">
            <div class="product-image ${product.bgClass}">
                ${product.imageText}
            </div>

            <h3 class="product-title">${product.name}</h3>
            <p class="origin-price">${product.originPrice}</p>
            <p class="sale-price">${product.salePrice}</p>

            <div class="card-footer">
                <span>${product.icon}</span>
            </div>
        </article>
    `).join("");
}

renderProducts("personalProductList", personalProducts);
renderProducts("adminProductList", adminProducts);

async function signup() {
    const email = document.getElementById("signupEmail").value.trim();
    const password = document.getElementById("signupPassword").value.trim();
    const passwordCheck = document.getElementById("signupPasswordCheck").value.trim();
    const name = document.getElementById("signupName").value.trim();
    const termsAgree = document.getElementById("termsAgree").checked;

    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }

    if (!password) {
        alert("비밀번호를 입력해주세요.");
        return;
    }

    if (password.length < 8) {
        alert("비밀번호는 8자 이상이어야 합니다.");
        return;
    }

    if (password !== passwordCheck) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    if (!name) {
        alert("이름을 입력해주세요.");
        return;
    }

    if (!termsAgree) {
        alert("약관에 동의해주세요.");
        return;
    }

    try {
        const response = await fetch("/api/v1/auth/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                password,
                name
            })
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "회원가입에 실패했습니다.");
            return;
        }

        showSignupComplete();

    } catch (error) {
        console.error(error);
        alert("서버와 연결할 수 없습니다.");
    }
}

function showSignupComplete() {
    const formSection = document.getElementById("signupFormSection");
    const completeSection = document.getElementById("signupCompleteSection");

    formSection.classList.add("hidden");
    completeSection.classList.remove("hidden");
}

async function login() {
    const email = document.getElementById("loginEmail").value.trim();
    const password = document.getElementById("loginPassword").value.trim();

    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }

    if (!password) {
        alert("비밀번호를 입력해주세요.");
        return;
    }

    try {
        const response = await fetch("/api/v1/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "로그인에 실패했습니다.");
            return;
        }

        const accessToken = result.data.accessToken;
        localStorage.setItem("accessToken", accessToken);

        alert("로그인에 성공했습니다.");
        location.href = "./index.html";

    } catch (error) {
        console.error(error);
        alert("서버와 연결할 수 없습니다.");
    }
}

function goRegisterPage() {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        alert("상품 등록은 로그인 후 이용할 수 있습니다.");
        location.href = "./login.html";
        return;
    }

    location.href = "./register.html";
}

function previewImage(event) {
    const file = event.target.files[0];
    const preview = document.getElementById("imagePreview");

    if (!file || !preview) {
        return;
    }

    const reader = new FileReader();

    reader.onload = function (e) {
        preview.innerHTML = `<img src="${e.target.result}" alt="상품 이미지">`;
    };

    reader.readAsDataURL(file);
}

async function submitGifticon() {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        alert("로그인이 필요합니다.");
        location.href = "./login.html";
        return;
    }

    const gifticonName = document.getElementById("gifticonName").value.trim();
    const originalPrice = document.getElementById("originalPrice").value.trim();
    const salePrice = document.getElementById("salePrice").value.trim();
    const pinNumber = document.getElementById("pinNumber").value.trim();
    const expiredAt = document.getElementById("expiredAt").value;
    const description = document.getElementById("description").value.trim();
    const image = document.getElementById("gifticonImage").files[0];

    if (!gifticonName) {
        alert("기프트카드 이름을 입력해주세요.");
        return;
    }

    if (!originalPrice) {
        alert("금액을 입력해주세요.");
        return;
    }

    if (!salePrice) {
        alert("판매가를 입력해주세요.");
        return;
    }

    if (!pinNumber) {
        alert("핀 번호를 입력해주세요.");
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

    formData.append("gifticonName", gifticonName);
    formData.append("originalPrice", originalPrice);
    formData.append("salePrice", salePrice);
    formData.append("pinNumber", pinNumber);
    formData.append("expiredAt", expiredAt);
    formData.append("description", description);

    if (image) {
        formData.append("image", image);
    }

    try {
        const response = await fetch("/api/v1/products", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            },
            body: formData
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            alert(result.message || "상품 등록에 실패했습니다.");
            return;
        }

        alert("상품이 등록되었습니다.");
        location.href = "./index.html";

    } catch (error) {
        console.error(error);
        alert("서버와 연결할 수 없습니다.");
    }
}

async function logout() {
    const token = localStorage.getItem("accessToken");

    if (!token) {
        location.href = "./login.html";
        return;
    }

    try {
        await fetch("/api/v1/auth/logout", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
    } catch (error) {
        console.error(error);
    }

    localStorage.removeItem("accessToken");
    alert("로그아웃되었습니다.");
    location.href = "./index.html";
}

function handleMainSearchEnter(event) {
    if (event.key === "Enter") {
        moveMainSearchPage();
    }
}

function moveMainSearchPage() {
    const input = document.getElementById("mainSearchInput");

    if (!input) {
        return;
    }

    const keyword = input.value.trim();

    if (!keyword) {
        alert("검색어를 입력해주세요.");
        return;
    }

    location.href = `./search.html?keyword=${encodeURIComponent(keyword)}`;
}


const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", () => {
    fetch("/users/info", {
        headers: { Authorization: token }
    })
        .then(res => res.json())
        .then(user => {
            const profileImg = document.getElementById("profile-image");
            if (user.profileImagePath) {
                profileImg.src = user.profileImagePath;
            }
        })
        .catch(err => console.error("회원 정보 로딩 실패:", err));
});

const partsBrandMap = {
    Adro: [
        { name: '휠', code: 'wh' },
        { name: '프론트 범퍼', code: 'fb' },
        { name: '사이드 스커트', code: 'ss' }
    ],
    Vörsteiner: [
        { name: '휠', code: 'wh' },
        { name: '리어 디퓨저', code: 'rd' }
    ],
    AKRAPOVIČ: [
        { name: '리어 디퓨저', code: 'rd' },
        { name: '윙', code: 'wi' }
    ]
};

const brandCodeMap = {
    Adro: '1',
    Vörsteiner: '2',
    AKRAPOVIČ: '3'
};

const partTypeNameMap = {
    wh: '휠',
    fb: '프론트 범퍼',
    ss: '사이드 스커트',
    rd: '리어 디퓨저',
    wi: '윙'
};

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('manufacturerList');
    Object.keys(partsBrandMap).forEach(name => {
        const div = document.createElement('div');
        div.className = 'manufacturer-item';
        div.textContent = name;
        div.onclick = event => selectManufacturer(name, event);
        container.appendChild(div);
    });
});

function selectManufacturer(brand, event) {
    document.getElementById('searchBtn').disabled = true;
    document.querySelectorAll('.manufacturer-item').forEach(item => item.classList.remove('selected'));
    event.target.classList.add('selected');

    const container = document.getElementById('modelContainer');
    container.innerHTML = '';
    (partsBrandMap[brand] || []).forEach(part => {
        const div = document.createElement('div');
        div.className = 'model-item';
        div.textContent = part.name;
        div.onclick = () => selectModel(brand, part, div);
        container.appendChild(div);
    });
}

function selectModel(brand, part, element) {
    document.querySelectorAll('.model-item').forEach(item => item.classList.remove('selected'));
    element.classList.add('selected');
    document.getElementById('searchBtn').disabled = false;

    localStorage.setItem("selectedBrand", brand);
    localStorage.setItem("selectedPart", JSON.stringify(part));
}

function toggleCartBox() {
    const box = document.getElementById("cartBox");
    box.style.display = box.style.display === "block" ? "none" : "block";
    renderCart();
}

function renderCart() {
    const list = document.getElementById("cartItems");
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    list.innerHTML = Object.entries(cart).map(([code, value]) => {
        const brandCode = value.charAt(0);
        const partCode = value.slice(1);
        const partName = partTypeNameMap[partCode] || partCode;
        const brandName = Object.entries(brandCodeMap).find(([_, c]) => c === brandCode)?.[0] || 'Unknown';
        return `<li><strong>${partName}</strong> - ${brandName} <button onclick="removeFromCart('${partCode}')" style="float: right; border: none; background: none; color: red; cursor: pointer;">✕</button></li>`;
    }).join("");
}

function removeFromCart(partType) {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    delete cart[partType];
    localStorage.setItem("carCart", JSON.stringify(cart));
    renderCart();
}

function clearCart() {
    if (confirm("장바구니를 모두 비우시겠습니까?")) {
        localStorage.removeItem("carCart");
        renderCart();
    }
}

function goToCart() {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    if (Object.keys(cart).length === 0) {
        alert("선택된 파츠가 없습니다.");
        return;
    }
    window.location.href = "/shopping-list";
}

async function searchCar() {
    const selectedBrand = localStorage.getItem("selectedBrand");
    const part = JSON.parse(localStorage.getItem("selectedPart"));
    const selectedCarManufacturer = localStorage.getItem("selectedManufacturer");
    const selectedCarModel = localStorage.getItem("selectedModel");
    const selectedCarCode = localStorage.getItem("selectedCarCode") || `${selectedCarManufacturer}_${selectedCarModel}`;

    const queryParams = new URLSearchParams({
        carManufacturer: selectedCarManufacturer,
        carModelCode: selectedCarModel,
        partBrand: selectedBrand,
        partType: part.name
    });

    const response = await fetch(`/tuning/2d/parts?${queryParams.toString()}`);
    const imagePaths = await response.json();

    const container = document.getElementById("imageContainer");
    container.innerHTML = '';

    imagePaths.forEach(imagePath => {
        const card = document.createElement('div');
        card.className = 'car-card';

        const img = document.createElement('img');
        img.src = imagePath;

        const name = document.createElement('div');
        name.className = 'car-name';
        const fileName = imagePath.split('/').pop();
        const namePart = fileName.split('_')[1]?.split('.')[0] || fileName.split('.')[0];
        name.textContent = namePart;

        const detail = document.createElement('div');
        detail.className = 'car-detail';
        detail.textContent = selectedBrand;

        card.appendChild(img);
        card.appendChild(name);
        card.appendChild(detail);

        const partCode = part.code;
        const partName = part.name;
        const brandCode = brandCodeMap[selectedBrand];
        const key = partCode;
        const value = fileName.split('.')[0];

        let cart = JSON.parse(localStorage.getItem("carCart") || "{}");

        if (cart[key] === value) {
            card.classList.add("selected");
        }

        card.onclick = () => {
            let prevCart = JSON.parse(localStorage.getItem("carCart") || "{}");
            let updatedCart = {};

            // car 키는 유지
            if (prevCart.car) {
                updatedCart.car = prevCart.car;
            }

            // 기존 파츠 key 유지 (car 제외)
            Object.entries(prevCart).forEach(([k, v]) => {
                if (k !== "car" && k !== key) {
                    updatedCart[k] = v;
                }
            });

            // 클릭한 이미지가 이미 선택된 경우 → 해제
            if (prevCart[key] === value) {
                document.querySelectorAll('.car-card').forEach(c => c.classList.remove("selected"));
            } else {
                // 선택 상태로 갱신
                updatedCart[key] = value;
                document.querySelectorAll('.car-card').forEach(c => c.classList.remove("selected"));
                card.classList.add("selected");
            }

            localStorage.setItem("carCart", JSON.stringify(updatedCart));
            renderCart();
        };
        container.appendChild(card);
    });
}


// 이어서 튜닝하기 버튼 클릭
document.getElementById("proceedBtn").addEventListener("click", async () => {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const entries = Object.entries(cart).filter(([key, _]) => key !== "car");   // 차량 제외하고 parts들만 가져오기

    if (entries.length === 0) {
        alert("저장할 파츠가 없습니다.");
        return;
    }

    for (const [_, value] of entries) {
        try {
            const res = await fetch("/shopping/add", {
                method: "POST",
                headers: {
                    "Authorization": token,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ imageCode: value }) // value는 예: "1wh_a"
            });

            if (!res.ok) {
                console.error("저장 실패:", value);
            }
        } catch (err) {
            console.error("요청 에러:", err);
        }
    }

    // 저장이 끝난 후 페이지 이동
    window.location.href = "/partsList";
});
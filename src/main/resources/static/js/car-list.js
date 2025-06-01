// 사용자 프로필 이미지 설정
const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", () => {
    fetch("/users/info", {
        headers: {
            Authorization: token
        }
    })
        .then(res => {
            if (!res.ok) throw new Error("회원 정보 조회 실패");
            return res.json();
        })
        .then(user => {
            // 프로필 이미지 반영
            const profileImg = document.getElementById("profile-image");
            if (user.profileImagePath) {
                profileImg.src = user.profileImagePath;
            }
        })
        .catch(err => {
            console.error("회원 정보 로딩 실패:", err);
            alert("회원 정보를 불러오는 데 실패했습니다.");
        });
});


let selectedManufacturer = '';
let selectedModel = '';


const manufacturerNames = ['현대', '기아', 'BMW', 'Audi', 'Benz', 'Toyota', 'Honda', 'Nissan', 'Chevrolet', 'Genesis', 'Volkswagen', 'Peugeot', 'Volvo', 'Ford', 'Chrysler', 'Jeep', 'Renault', 'Mazda', 'Lexus', 'Infiniti'];

const modelMap = {
    현대: ['아반떼', '쏘나타', '투싼', '싼타페', '펠리세이드'],
    기아: ['K3', 'K5', 'K7', '쏘렌토', '스포티지'],
    BMW: ['i7', 'i5', 'iX3', 'i4', 'iX2', 'm4-g82'],
    Audi: ['A3', 'A4', 'Q5', 'Q7', 'e-tron'],
    Benz: ['C클래스', 'E클래스', 'GLC', 'GLE', 'EQE'],
    Toyota: ['Corolla', 'Camry', 'RAV4', 'Prius', 'Supra'],
    Honda: ['Civic', 'Accord', 'CR-V', 'HR-V', 'Insight'],
    Nissan: ['Altima', 'Maxima', 'Rogue', 'Murano', 'Leaf'],
    Chevrolet: ['Spark', 'Malibu', 'Trax', 'Equinox', 'Bolt'],
    Genesis: ['G70', 'G80', 'G90', 'GV70', 'GV80'],
    Volkswagen: ['Golf', 'Passat', 'Tiguan', 'Arteon', 'ID.4'],
    Peugeot: ['208', '308', '2008', '3008', '5008'],
    Volvo: ['XC40', 'XC60', 'XC90', 'S60', 'V60'],
    Ford: ['Focus', 'Fusion', 'Escape', 'Explorer', 'Mustang'],
    Chrysler: ['300', 'Pacifica', 'Voyager', 'Sebring', 'Aspen'],
    Jeep: ['Wrangler', 'Cherokee', 'Compass', 'Gladiator', 'Renegade'],
    Renault: ['Clio', 'Megane', 'Captur', 'Arkana', 'Talisman'],
    Mazda: ['Mazda2', 'Mazda3', 'CX-3', 'CX-5', 'MX-30'],
    Lexus: ['IS', 'ES', 'NX', 'RX', 'UX'],
    Infiniti: ['Q50', 'Q60', 'QX50', 'QX60', 'QX80']
};

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('manufacturerList');
    manufacturerNames.forEach(name => {
        const div = document.createElement('div');
        div.className = 'manufacturer-item';
        div.textContent = name;
        div.onclick = event => selectManufacturer(name, event);
        container.appendChild(div);
    });
});

function selectManufacturer(manufacturer, event) {
    selectedManufacturer = manufacturer;
    selectedModel = '';
    document.getElementById('searchBtn').disabled = true;
    document.querySelectorAll('.manufacturer-item').forEach(item => item.classList.remove('selected'));
    event.target.classList.add('selected');
    const container = document.getElementById('modelContainer');
    container.innerHTML = '';
    (modelMap[manufacturer] || []).forEach(model => {
        const div = document.createElement('div');
        div.className = 'model-item';
        div.textContent = model;
        div.onclick = () => selectModel(model, div);
        container.appendChild(div);
    });
}

function selectModel(model, element) {
    selectedModel = model;
    document.querySelectorAll('.model-item').forEach(item => item.classList.remove('selected'));
    element.classList.add('selected');
    if (selectedManufacturer && selectedModel) {
        document.getElementById('searchBtn').disabled = false;
    }
}

function toggleCartBox() {
    const box = document.getElementById("cartBox");
    box.style.display = box.style.display === "block" ? "none" : "block";
    renderCart();
}

function renderCart() {
    const list = document.getElementById("cartItems");
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");

    list.innerHTML = Object.entries(cart).map(([key, value]) => `
        <li>
            ${key}: ${value}
            <button onclick="removeFromCart('${key}')" style="float: right; border: none; background: none; color: red; cursor: pointer;">✕</button>
        </li>
    `).join("");
}


function removeFromCart(key) {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    delete cart[key];
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
    const imageCode = cart["car"];
    if (!imageCode) {
        alert("선택된 차량이 없습니다.");
        return;
    }

    const token = localStorage.getItem("token");

    fetch("/shopping/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`
        },
        body: JSON.stringify({ imageCode })
    })
        .then(res => {
            if (!res.ok) throw new Error("장바구니 등록 실패");
            window.location.href = "/parts-list";
        })
        .catch(err => {
            console.error("장바구니 등록 실패:", err);
            alert("차량 정보를 서버에 저장하는 데 실패했습니다.");
        });
}




async function searchCar() {
    if (!selectedManufacturer || !selectedModel) return;

    const url = `/tuning/2d?manufacturer=${selectedManufacturer}&modelCode=${selectedModel}&category=car`;
    const response = await fetch(url);
    const images = await response.json();

    const container = document.getElementById("imageContainer");
    container.innerHTML = '';

    // localStorage에 저장된 차량명 (예: car:bmw_m4_g82_white)
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const selectedName = cart["car"]; // ex: "bmw_m4_g82_white"


    images.forEach(src => {
        const card = document.createElement('div');
        card.className = 'car-card';

        // 차량 파일명 추출
        const fileName = src.split('/car/')[1]?.split('.png')[0] || 'Unknown';
        const carName = fileName;

        // 선택된 차량이면 강조
        if (selectedName === carName) {
            card.classList.add("selected");
        }

        const img = document.createElement('img');
        img.src = src;

        const name = document.createElement('div');
        name.className = 'car-name';
        name.textContent = carName;

        const detail = document.createElement('div');
        detail.className = 'car-detail';
        detail.textContent = '순수 전기';

        card.appendChild(img);
        card.appendChild(name);
        card.appendChild(detail);

        // 차량 이미지 클릭 시 처리
        card.onclick = () => {
            const isSelected = card.classList.contains("selected");

            if (isSelected) {
                card.classList.remove("selected");

                const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
                delete cart["car"];
                localStorage.setItem("carCart", JSON.stringify(cart));
            } else {
                document.querySelectorAll('.car-card.selected').forEach(c => c.classList.remove('selected'));
                card.classList.add("selected");

                // 제조사/모델명 따로 저장
                localStorage.setItem("selectedManufacturer", selectedManufacturer);
                localStorage.setItem("selectedModel", selectedModel);
                const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
                cart["car"] = carName;
                localStorage.setItem("carCart", JSON.stringify(cart));
            }
        };

        container.appendChild(card);
    });
}
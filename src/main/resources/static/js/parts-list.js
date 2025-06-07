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
        });

    renderManufacturers();
});

const partsBrandMap = {
    Adro: ['wh', 'ss', 'rd', 'wi', 'fl'],
    Vörsteiner: ['wh', 'ss', 'rd', 'wi', 'fl'],
    AKRAPOVIČ: ['wh', 'ss', 'rd', 'wi', 'fl']
};

const brandCodeMap = {
    Adro: '1',
    Vörsteiner: '2',
    AKRAPOVIČ: '3'
};

const partTypeNameMap = {
    wh: '휠', ss: '사이드 스커트', rd: '리어 디퓨저', wi: '윙', fl: '프론트립'
};

let selectedBrand = '';
let selectedPartCode = '';

function renderManufacturers() {
    const container = document.getElementById('manufacturerList');
    container.innerHTML = '';
    Object.keys(partsBrandMap).forEach(brand => {
        const div = document.createElement('div');
        div.className = 'manufacturer-item';
        div.textContent = brand;
        div.onclick = () => selectManufacturer(brand, div);
        container.appendChild(div);
    });
}

function selectManufacturer(brand, element) {
    selectedBrand = brand;
    selectedPartCode = '';
    document.getElementById("searchBtn").disabled = true;
    document.getElementById("step1").classList.add("selected");
    document.getElementById("step2").classList.remove("selected");

    document.querySelectorAll(".manufacturer-item").forEach(el => el.classList.remove("selected"));
    element.classList.add("selected");

    const container = document.getElementById('modelContainer');
    container.innerHTML = '';
    partsBrandMap[brand].forEach(partCode => {
        const div = document.createElement('div');
        div.className = 'model-item';
        div.textContent = partTypeNameMap[partCode];
        div.onclick = () => selectPart(partCode, div);
        container.appendChild(div);
    });
}

function selectPart(partCode, element) {
    selectedPartCode = partCode;
    document.getElementById("step2").classList.add("selected");
    document.getElementById("searchBtn").disabled = false;

    document.querySelectorAll(".model-item").forEach(el => el.classList.remove("selected"));
    element.classList.add("selected");
}

async function searchParts() {
    const manu = localStorage.getItem("selectedManufacturer");
    const model = localStorage.getItem("selectedModel");
    const codeName = localStorage.getItem("selectedCodeName");

    const brandCode = brandCodeMap[selectedBrand];
    const prefix = brandCode + selectedPartCode;

    const response = await fetch(`/tuning/parts?manufacturer=${manu}&model=${model}&codeName=${codeName}&partTypePrefix=${prefix}`);
    const imagePaths = await response.json();

    const container = document.getElementById("imageContainer");
    container.innerHTML = "";

    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const selectedValue = cart[selectedPartCode];

    imagePaths.forEach(path => {
        const fileName = path.split('/').pop();
        const baseName = fileName.split('.')[0];
        const partKey = selectedPartCode;
        const value = baseName;

        const card = document.createElement("div");
        card.className = "car-card";

        const img = document.createElement("img");
        img.src = `/uploads/2d/${manu}/${model}/${codeName}/parts/${fileName}`;
        card.appendChild(img);

        const name = document.createElement("div");
        name.className = "car-name";
        name.textContent = baseName;
        card.appendChild(name);

        if (value === selectedValue) {
            card.classList.add("selected");
        }

        card.onclick = () => {
            let cart = JSON.parse(localStorage.getItem("carCart") || "{}");
            const alreadySelected = cart[partKey] === value;

            if (alreadySelected) {
                if (confirm("선택된 파츠를 삭제하시겠습니까?")) {
                    delete cart[partKey];
                    card.classList.remove("selected");
                }
            } else if (cart[partKey]) {
                if (confirm("이미 해당 부위의 파츠가 선택되어 있습니다. 변경하시겠습니까?")) {
                    cart[partKey] = value;
                    document.querySelectorAll(".car-card").forEach(c => c.classList.remove("selected"));
                    card.classList.add("selected");
                }
            } else {
                cart[partKey] = value;
                document.querySelectorAll(".car-card").forEach(c => c.classList.remove("selected"));
                card.classList.add("selected");
            }

            localStorage.setItem("carCart", JSON.stringify(cart));
            renderCart();
        };

        container.appendChild(card);
    });
}

function renderCart() {
    const list = document.getElementById("cartItems");
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");

    list.innerHTML = Object.entries(cart)
        .filter(([k]) => k !== "car")
        .map(([k, v]) => `<li>${partTypeNameMap[k]}: ${v}
            <button onclick="removeFromCart('${k}')" style="float:right;border:none;background:none;color:red;">✕</button></li>`)
        .join("");
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

function toggleCartBox() {
    const box = document.getElementById("cartBox");
    box.style.display = box.style.display === "block" ? "none" : "block";
    renderCart();
}

async function goToCart() {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const entries = Object.entries(cart).filter(([key]) => key !== "car");

    if (entries.length === 0) {
        alert("파츠를 먼저 선택해주세요.");
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
                body: JSON.stringify({ imageCode: value })
            });

            if (!res.ok) {
                console.error("추가 실패:", value);
            }
        } catch (e) {
            console.error("요청 실패:", e);
        }
    }

    window.location.href = "/shopping-list";
}

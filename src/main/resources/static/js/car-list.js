
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

    renderManufacturerList();
    renderCart();
});

let selectedManufacturer = '';
let selectedModel = '';

const manufacturerNames = ['BMW', 'Audi', 'Benz'];
const modelMap = {
    BMW: ['m4-g82', 'i4'],
    Audi: ['A4', 'A6'],
    Benz: ['C클래스', 'E클래스']
};

function renderManufacturerList() {
    const container = document.getElementById('manufacturerList');
    container.innerHTML = '';
    manufacturerNames.forEach(name => {
        const div = document.createElement('div');
        div.className = 'manufacturer-item';
        div.textContent = name;
        div.onclick = event => selectManufacturer(name, event);
        container.appendChild(div);
    });
}

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
    document.getElementById('searchBtn').disabled = !(selectedManufacturer && selectedModel);
}

async function searchCar() {
    if (!selectedManufacturer || !selectedModel) return;

    const codeName = selectedModel.replace(/-/g, '').toLowerCase();
    const url = `/tuning/2d?manufacturer=${selectedManufacturer}&model=${selectedModel}&codeName=${codeName}`;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error("2D 이미지 불러오기 실패");
        const images = await response.json();

        const selectedRes = await fetch("/shopping/list", {
            headers: { "Authorization": token }
        });
        const selectedList = await selectedRes.json();
        const selectedCar = selectedList.find(item => item.code.split("_").length >= 4)?.code;

        const container = document.getElementById("imageContainer");
        container.innerHTML = '';

        images.forEach(src => {
            const card = document.createElement('div');
            card.className = 'car-card';
            const fileName = src.split('/').pop();
            const imageCode = fileName.replace(/\.(png|jpg|jpeg)/, '');
            if (selectedCar === imageCode) card.classList.add("selected");

            const img = document.createElement('img');
            img.src = src;
            const name = document.createElement('div');
            name.className = 'car-name';
            name.textContent = imageCode;

            card.appendChild(img);
            card.appendChild(name);

            card.onclick = () => {
                if (card.classList.contains("selected")) {
                    card.classList.remove("selected");
                    fetch("/shopping/remove", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": token
                        },
                        body: JSON.stringify({ imageCode })
                    });
                } else {
                    document.querySelectorAll('.car-card.selected').forEach(c => c.classList.remove('selected'));
                    card.classList.add("selected");
                    fetch("/shopping/add", {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "Authorization": token
                        },
                        body: JSON.stringify({ imageCode })
                    });
                }
            };

            container.appendChild(card);
        });
    } catch (err) {
        console.error("검색 실패:", err);
        alert("이미지를 불러오는 데 실패했습니다.");
    }
}

function renderCart() {
    fetch("/shopping/list", {
        headers: { "Authorization": token }
    })
        .then(res => res.json())
        .then(items => {
            const list = document.getElementById("cartItems");
            list.innerHTML = items.map(item => `
            <li>${item.label}
                <button onclick="removeFromCart('${item.code}')"
                        style="float:right; border:none; background:none; color:red; cursor:pointer;">✕</button>
            </li>
        `).join("");
        });
}

function removeFromCart(code) {
    fetch("/shopping/remove", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": token
        },
        body: JSON.stringify({ imageCode: code })
    }).then(renderCart);
}
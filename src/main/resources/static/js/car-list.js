// car-list.js 전체 코드

const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", () => {
    renderManufacturerList();
    renderCart();
});

let selectedManufacturer = '';
let selectedModel = '';
let selectedCodeName = '';

const manufacturerModelMap = {
    BMW: ['1시리즈', '2시리즈', '3시리즈', '4시리즈', 'M2', 'M3', 'M4'],
    BENZ: ['A클래스', 'C클래스', 'E클래스'],
    AUDI: ['A3', 'A5', 'A6', 'A7']
};

const modelCodeNameMap = {
    'M4': ['g82', 'f82'],
    '3시리즈': ['g20'],
    'A클래스': ['w177'],
    'A3': ['8Y']
};

function renderManufacturerList() {
    const container = document.getElementById('manufacturerList');
    container.innerHTML = '';
    Object.keys(manufacturerModelMap).forEach(name => {
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
    selectedCodeName = '';
    document.getElementById('searchBtn').disabled = true;
    document.querySelectorAll('.manufacturer-item').forEach(item => item.classList.remove('selected'));
    event.target.classList.add('selected');

    const modelContainer = document.getElementById('modelContainer');
    modelContainer.innerHTML = '';
    (manufacturerModelMap[manufacturer] || []).forEach(model => {
        const div = document.createElement('div');
        div.className = 'model-item';
        div.textContent = model;
        div.onclick = () => selectModel(model, div);
        modelContainer.appendChild(div);
    });

    document.getElementById('codeNameContainer').innerHTML = '';
}

function selectModel(model, element) {
    selectedModel = model;
    selectedCodeName = '';
    document.querySelectorAll('.model-item').forEach(item => item.classList.remove('selected'));
    element.classList.add('selected');

    const codeNameContainer = document.getElementById('codeNameContainer');
    codeNameContainer.innerHTML = '';
    (modelCodeNameMap[model] || []).forEach(code => {
        const div = document.createElement('div');
        div.className = 'model-item';
        div.textContent = code;
        div.onclick = () => selectCodeName(code, div);
        codeNameContainer.appendChild(div);
    });

    document.getElementById('searchBtn').disabled = true;
}

function selectCodeName(codeName, element) {
    selectedCodeName = codeName;
    document.querySelectorAll('#codeNameContainer .model-item').forEach(item => item.classList.remove('selected'));
    element.classList.add('selected');
    document.getElementById('searchBtn').disabled = false;
}

async function searchCar() {
    if (!selectedManufacturer || !selectedModel || !selectedCodeName) return;

    const url = `/tuning/2d?manufacturer=${selectedManufacturer}&model=${selectedModel}&codeName=${selectedCodeName}`;

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
            const carItems = items.filter(item => item.code.split("_").length >= 4); // 차량만 추출
            const list = document.getElementById("cartItems");
            if (list) {
                list.innerHTML = carItems.map(item => `
                <li>${item.label}
                    <button onclick="removeFromCart('${item.code}')"
                            style="float:right; border:none; background:none; color:red; cursor:pointer;">✕</button>
                </li>
            `).join("");
            }
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

function goToCart() {
    fetch("/shopping/list", {
        headers: {
            "Authorization": token
        }
    })
        .then(res => res.json())
        .then(list => {
            const hasCar = list.some(item => item.code.split("_").length >= 4);
            if (!hasCar) {
                alert("차량을 먼저 선택해주세요.");
                return;
            }
            window.location.href = "/parts-list";
        })
        .catch(err => {
            console.error("장바구니 확인 실패:", err);
            alert("장바구니 상태를 확인할 수 없습니다.");
        });
}

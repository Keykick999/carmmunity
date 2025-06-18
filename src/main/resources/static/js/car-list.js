const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", () => {
    fetch("/users/info", { headers: { Authorization: token } })
        .then(res => res.json())
        .then(user => {
            const profileImg = document.getElementById("profile-image");
            if (user.profileImagePath) {
                profileImg.src = user.profileImagePath;
            }
        });

    renderManufacturers();
});

const modelMap = {
    bmw: ["1시리즈", "2시리즈", "3시리즈", "4시리즈", "M2", "M3", "M4"],
    benz: ["A클래스", "C클래스", "E클래스"],
    audi: ["A3", "A5", "A6", "A7"]
};

const codeNameMap = {
    M4: ["g82", "f82", "e92"],
    M3: ["g80", "f80", "e90"]
};

let selectedManufacturer = "";
let selectedModel = "";
let selectedCodeName = "";

function renderManufacturers() {
    const container = document.getElementById("manufacturerList");
    container.innerHTML = "";
    Object.keys(modelMap).forEach(name => {
        const div = document.createElement("div");
        div.className = "model-item";
        div.textContent = name.toUpperCase();
        div.onclick = () => selectManufacturer(name, div);
        container.appendChild(div);
    });
}

function selectManufacturer(name, element) {
    selectedManufacturer = name;
    selectedModel = "";
    selectedCodeName = "";
    document.getElementById("searchBtn").disabled = true;

    document.querySelectorAll("#manufacturerList .model-item").forEach(el => el.classList.remove("selected"));
    element.classList.add("selected");

    renderModels(name);
    document.getElementById("codeNameContainer").innerHTML = "<p>코드네임을 선택해주세요.</p>";
}


function renderModels(manufacturer) {
    const container = document.getElementById("modelContainer");
    container.innerHTML = "";
    (modelMap[manufacturer] || []).forEach(model => {
        const div = document.createElement("div");
        div.className = "model-item";
        div.textContent = model;
        div.onclick = () => selectModel(model, div);
        container.appendChild(div);
    });
}

function selectModel(model, element) {
    selectedModel = model;
    selectedCodeName = "";
    document.getElementById("searchBtn").disabled = true;

    document.querySelectorAll("#modelContainer .model-item").forEach(el => el.classList.remove("selected"));
    element.classList.add("selected");

    renderCodeNames(model);
}


function renderCodeNames(model) {
    const container = document.getElementById("codeNameContainer");
    container.innerHTML = "";
    (codeNameMap[model] || []).forEach(code => {
        const div = document.createElement("div");
        div.className = "model-item";
        div.textContent = code.toUpperCase();
        div.onclick = () => selectCodeName(code, div);
        container.appendChild(div);
    });
}

function selectCodeName(codeName, element) {
    selectedCodeName = codeName;
    document.querySelectorAll("#codeNameContainer .model-item").forEach(el => el.classList.remove("selected"));
    element.classList.add("selected");

    if (selectedManufacturer && selectedModel && selectedCodeName) {
        document.getElementById("searchBtn").disabled = false;
    }
}

async function searchCarImages() {
    const manu = selectedManufacturer.toLowerCase();
    const model = selectedModel.toLowerCase();
    const code = selectedCodeName.toLowerCase();

    const response = await fetch(`/tuning/cars?manufacturer=${manu}&model=${model}&codeName=${code}`);
    const imageNames = await response.json();

    const container = document.getElementById("imageContainer");
    container.innerHTML = "";

    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const selectedCar = cart["car"];

    imageNames.forEach(name => {
        const card = document.createElement("div");
        card.className = "car-card";

        const img = document.createElement("img");
        img.src = `/uploads/2d/${manu}/${model}/${code}/cars/${name}`;
        img.alt = name;

        const label = document.createElement("div");
        label.className = "car-name";
        label.textContent = name.replace(".png", "");

        if (selectedCar === name.replace(".png", "")) {
            card.classList.add("selected");
        }

        card.appendChild(img);
        card.appendChild(label);

        card.onclick = () => {
            const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
            const current = cart["car"];
            const clicked = name.replace(".png", "");
            const isSame = current === clicked;

            if (isSame) {
                if (confirm("선택된 차량을 삭제하시겠습니까?")) {
                    localStorage.removeItem("carCart");
                    card.classList.remove("selected");
                }
            } else if (current) {
                if (confirm("이미 차량이 선택되어 있습니다. 차량을 변경하시겠습니까?")) {
                    document.querySelectorAll(".car-card").forEach(c => c.classList.remove("selected"));
                    card.classList.add("selected");
                    localStorage.setItem("carCart", JSON.stringify({ car: clicked }));
                    localStorage.setItem("selectedManufacturer", manu);
                    localStorage.setItem("selectedModel", model);
                    localStorage.setItem("selectedCodeName", code);
                }
            } else {
                card.classList.add("selected");
                localStorage.setItem("carCart", JSON.stringify({ car: clicked }));
                localStorage.setItem("selectedManufacturer", manu);
                localStorage.setItem("selectedModel", model);
                localStorage.setItem("selectedCodeName", code);
            }
        };

        container.appendChild(card);
    });
}

function goToPartsList() {
    const cart = JSON.parse(localStorage.getItem("carCart") || "{}");
    const imageCode = cart["car"];

    if (!imageCode) {
        alert("차량을 먼저 선택해주세요.");
        return;
    }

    fetch("/shopping/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Authorization": token
        },
        body: JSON.stringify({ imageCode })
    })
        .then(res => {
            if (!res.ok) throw new Error("장바구니 등록 실패");
            window.location.href = "/parts-list";
        })
        .catch(err => {
            console.error("등록 실패:", err);
            alert("차량 등록에 실패했습니다.");
        });
}

const token = 'Bearer ' + localStorage.getItem("token");

if (!token || token === "Bearer null") {
    alert("로그인이 필요합니다.");
    window.location.href = "/";
}

document.addEventListener("DOMContentLoaded", () => {
    fetch("/shopping/list", {
        headers: {
            "Authorization": token
        }
    })
        .then(res => res.json())
        .then(items => {
            renderCart(items);
        })
        .catch(err => {
            console.error("장바구니 로딩 실패:", err);
            alert("장바구니 정보를 불러오는 데 실패했습니다.");
        });
});

function renderCart(items) {
    const carInfo = document.getElementById('car-info');
    const carImage = document.getElementById('car-image');
    const partsSection = document.getElementById('parts-section');

    const carItem = items.find(item => item.code.split("_").length >= 4);
    const partItems = items.filter(item => item.code.split("_").length < 4);

    if (carItem) {
        const parts = carItem.code.split("_");
        const manufacturer = parts[0];
        const model = parts[1];
        const codeName = parts[2];
        const color = parts[3];

        carInfo.innerHTML = `
            <p><strong>제조사:</strong> ${manufacturer.toUpperCase()}</p>
            <p><strong>모델:</strong> ${model.toUpperCase()}</p>
            <p><strong>코드네임:</strong> ${codeName.toUpperCase()}</p>
            <p><strong>차량명:</strong> ${carItem.label}</p>
        `;
        carImage.src = `/uploads/2d/${selectedManufacturer}/${selectedModel}/${codeName}/${carItem.code}.png`;
        console.log(carImage.src);
    }

    partItems.forEach(item => {
        const brandCode = item.code[0];
        const typeCode = item.code.slice(1, 3);
        const brandMap = { "1": "Adro", "2": "Vörsteiner", "3": "AKRAPOVIČ" };
        const partMap = {
            wh: '휠',
            ss: '사이드 스커트',
            rd: '리어 디퓨저',
            wi: '윙',
            fl: '프론트립'
        };

        const brand = brandMap[brandCode] || "Unknown";
        const type = partMap[typeCode] || "Unknown";

        const carParts = carItem?.code?.split("_") ?? ["unknown", "unknown", "unknown"];
        const imagePath = `/uploads/2d/${carParts[0]}/${carParts[1]}/${carParts[2]}/${item.code}.png`;

        const partCard = document.createElement('div');
        partCard.className = 'part-card';
        partCard.innerHTML = `
            <img src="${imagePath}" alt="${item.code}" />
            <div class="part-info">
                <p><strong>브랜드:</strong> ${brand}</p>
                <p><strong>종류:</strong> ${type}</p>
                <p><strong>이름:</strong> ${item.label}</p>
            </div>
        `;
        partsSection.appendChild(partCard);
    });
}
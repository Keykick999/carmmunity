import * as THREE from 'three';
import { GLTFLoader } from 'GLTFLoader';
import { OrbitControls } from 'OrbitControls';

const container = document.querySelector('.viewer-container');
const loadingEl = document.getElementById('loading');
const camera = new THREE.OrthographicCamera();
const renderer = new THREE.WebGLRenderer({ antialias: true });
const controls = new OrbitControls(camera, renderer.domElement);
let currentImagePath = '';
let currentModel = null;

const d = 4;
const width = container.clientWidth;
const height = container.clientHeight;
const aspect = width / height;

camera.left = -d * aspect;
camera.top = d;
camera.bottom = -d;
camera.near = 0.1;
camera.far = 100;
camera.position.set(0, 1.5, 14);
camera.lookAt(0, 0, 0);

renderer.setSize(width, height);
renderer.setPixelRatio(window.devicePixelRatio);
renderer.outputEncoding = THREE.sRGBEncoding;
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;
renderer.toneMapping = THREE.ACESFilmicToneMapping;
renderer.toneMappingExposure = 1.25;




container.appendChild(renderer.domElement);

const scene = new THREE.Scene();
scene.background = new THREE.Color(0xffffff);


// 바닥 Plane
const planeGeo = new THREE.PlaneGeometry(100, 100);
const planeMat = new THREE.MeshStandardMaterial({
    color: 0xf0f0f0,
    metalness: 0.3,
    roughness: 0.4
});
const plane = new THREE.Mesh(planeGeo, planeMat);
plane.rotation.x = -Math.PI / 2;
plane.position.y = -2;
plane.receiveShadow = true;
scene.add(plane);

// 조명
const keyLight = new THREE.DirectionalLight(0xfff2e5, 1.7);
keyLight.position.set(10, 10, 10);
scene.add(keyLight);

const rimLight = new THREE.DirectionalLight(0xe0e0ff, 0.8);
rimLight.position.set(-10, 5, -8);
scene.add(rimLight);

const spotLight = new THREE.SpotLight(0xffffff, 1.2);
spotLight.position.set(5, 10, 5);
spotLight.target.position.set(0, 0, 0);
scene.add(spotLight, spotLight.target);


controls.enableDamping = true;
controls.enableZoom = false;
controls.minPolarAngle = Math.PI / 2;
controls.maxPolarAngle = Math.PI / 2;

const loader = new GLTFLoader();

fetch('/shopping/compose', {
    method: 'POST',
    headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
})
    .then(res => res.json())
    .then(data => {
        currentImagePath = data.glbPath;

        return fetch(data.glbPath).then(res => res.arrayBuffer());
    })
    .then(buffer => {
        loader.parse(buffer, '', gltf => {
            if (currentModel) scene.remove(currentModel);
            currentModel = gltf.scene;

            // 각 child에 재질 및 그림자 설정
            currentModel.traverse(child => {
                if (child.isMesh) {
                    if (child.material && child.material.isMeshStandardMaterial) {
                        child.material.metalness = 0.7;
                        child.material.roughness = 0.3;
                        child.material.envMapIntensity = 1.2;
                    }
                    child.castShadow = true;
                    child.receiveShadow = true;
                }
            });


            const box = new THREE.Box3().setFromObject(currentModel);
            const center = box.getCenter(new THREE.Vector3());
            const size = box.getSize(new THREE.Vector3());

            currentModel.position.sub(center);
            currentModel.position.y -= size.y * 225.0;

            // 3D 모델 크기를 0에서 시작 → 부드럽게 키우기
            currentModel.scale.set(0.01, 0.01, 0.01);
            scene.add(currentModel);

            // 등장 애니메이션
            const targetScale = new THREE.Vector3(350, 350, 350);
            let progress = 0;
            const scaleUp = () => {
                if (progress < 1) {
                    progress += 0.02;
                    currentModel.scale.lerp(targetScale, 0.1);
                    requestAnimationFrame(scaleUp);
                } else {
                    currentModel.scale.copy(targetScale);
                }
            };
            scaleUp();

            // 회전 중심 설정
            controls.target.set(0, -size.y * 0.3, 0);
            controls.update();

            // 로딩 화면 제거
            loadingEl.style.display = 'none';

            // 리사이즈 반영
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    const width = container.clientWidth;
                    const height = container.clientHeight;
                    renderer.setSize(width, height);
                    camera.left = -d * (width / height);
                    camera.right = d * (width / height);
                    camera.updateProjectionMatrix();
                });
            });
        });
    })
    .catch(err => {
        console.error('모델 로딩 실패:', err);
        loadingEl.innerText = '🚫 모델 로드 실패';
    });

function animate() {
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene, camera);
}
animate();

window.addEventListener('resize', () => {
    const aspect = container.clientWidth / container.clientHeight;
    camera.left = -d * aspect;
    camera.right = d * aspect;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
});

// 버튼 이벤트
document.getElementById('retune-btn').onclick = () => {
    location.href = '/car-list';
};

const saveModal = document.getElementById('save-modal');

function disableSlotsExcept(slotNum) {
    document.querySelectorAll('input[name="slot"]').forEach(input => {
        input.disabled = input.value !== String(slotNum);
    });
}

function enableAllSlots() {
    document.querySelectorAll('input[name="slot"]').forEach(input => {
        input.disabled = false;
    });
}

document.getElementById('save-btn').onclick = async () => {
    const res = await fetch('/cars/list', {
        headers: { Authorization: 'Bearer ' + localStorage.getItem('token') }
    });
    const cars = await res.json();

    if (!cars || cars.length === 0) {
        disableSlotsExcept(1);
        alert("첫 번째 튜닝 차량은 1번에만 저장이 가능합니다.");
    } else {
        enableAllSlots();
    }
    saveModal.classList.remove('hidden');
    document.querySelectorAll('.slot-box').forEach(label => {
        label.addEventListener('click', () => {
            document.querySelectorAll('.slot-box').forEach(l => l.classList.remove('selected'));
            label.classList.add('selected');
            label.querySelector('input').checked = true;
        });
    });
};

document.getElementById('confirm-save').onclick = async () => {
    const selected = document.querySelector('input[name="slot"]:checked');
    if (!selected) return alert("슬롯을 선택하세요");

    const carId = parseInt(selected.value);
    const res = await fetch('/cars', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({ carId, imagePath: currentImagePath })
    });

    if (res.ok) {
        location.href = '/main';
    } else {
        console.error(await res.text());
        alert('저장 실패');
    }
};

document.getElementById('close-modal').onclick = () => {
    saveModal.classList.add('hidden');
};
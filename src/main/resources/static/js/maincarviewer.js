import * as THREE from 'three';
import { GLTFLoader } from 'GLTFLoader';
import { OrbitControls } from 'OrbitControls';



// 3D Viewer (생략 없이 유지)
const scene = new THREE.Scene();
scene.background = new THREE.Color(0xf5f6fa);
const loader = new THREE.TextureLoader();
// loader.load('/garage.jpg', (texture) => {
//     scene.background = texture;
// });

const camera = new THREE.PerspectiveCamera(75, 900 / 500, 0.1, 1000);
camera.position.set(0, 2, 20);

const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(900, 500);
document.querySelector('.viewer-container').appendChild(renderer.domElement);

const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;

scene.add(new THREE.AmbientLight(0xffffff, 1.8));
const dirLight = new THREE.DirectionalLight(0xffffff, 1);
dirLight.position.set(5, 10, 7.5);
scene.add(dirLight);

let currentModel;
const token = 'Bearer ' + localStorage.getItem("token");
window.loadCar = function (slotId) {
    const modelUrl = `http://localhost:8080/cars?carId=${slotId}`;
    const loader = new GLTFLoader();

    fetch(modelUrl, {
        headers: { Authorization: token }
    })
        .then(res => {
            if (!res.ok) throw new Error("Fetch 실패: " + res.status);
            return res.arrayBuffer();
        })
        .then(buffer => {
            if (currentModel) scene.remove(currentModel);
            loader.parse(buffer, '', (gltf) => {
                currentModel = gltf.scene;
                scene.add(gltf.scene);
            });
        })
        .catch(err => console.error('모델 로딩 오류:', err));
};

function animate() {
    requestAnimationFrame(animate);
    controls.update();
    renderer.render(scene, camera);
}
animate();
loadCar(1);

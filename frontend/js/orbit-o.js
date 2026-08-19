// js/orbit-o.js
import * as THREE from "three";

const BASE_COLORS = [
  new THREE.Color(0x07111b), // near-black blue
  new THREE.Color(0x102235), // deep steel blue
  new THREE.Color(0x18344d), // muted blue
  new THREE.Color(0x1f5fa8), // existing electric blue accent
  new THREE.Color(0xd8e6f2), // small cold highlight
];

export function createOrbitO() {
  // Keep these values the same as your original O if you already tuned its shape.
  const geometry = new THREE.TorusGeometry(1.42, 0.205, 18, 120).toNonIndexed();

  const colorAttr = new Float32Array(geometry.attributes.position.count * 3);
  const faceData = [];

  for (let i = 0; i < geometry.attributes.position.count; i += 3) {
    const colorIndex = weightedColorIndex();
    const color = BASE_COLORS[colorIndex].clone();

    const pulse = Math.random() * Math.PI * 2;
    const speed = 0.45 + Math.random() * 0.75;
    const strength = 0.06 + Math.random() * 0.16;

    faceData.push({
      colorIndex,
      pulse,
      speed,
      strength,
    });

    for (let j = 0; j < 3; j++) {
      color.toArray(colorAttr, (i + j) * 3);
    }
  }

  geometry.setAttribute("color", new THREE.BufferAttribute(colorAttr, 3));
  geometry.computeVertexNormals();

  const material = new THREE.MeshPhysicalMaterial({
    vertexColors: true,
    flatShading: true,
    metalness: 0.52,
    roughness: 0.38,
    clearcoat: 0.35,
    clearcoatRoughness: 0.5,
    emissive: new THREE.Color(0x071522),
    emissiveIntensity: 0.22,
    transparent: true,
    opacity: 0.96,
  });

  const mesh = new THREE.Mesh(geometry, material);

  // Keep your current O proportions.
  mesh.scale.set(1.12, 1, 1);
  mesh.rotation.x = -0.05;

  return {
    mesh,
    geometry,
    material,
    faceData,
  };
}

export function updateOrbitO(orbitO, elapsedTime) {
  const colors = orbitO.geometry.attributes.color;
  const colorArray = colors.array;

  for (let face = 0; face < orbitO.faceData.length; face++) {
    const data = orbitO.faceData[face];
    const base = BASE_COLORS[data.colorIndex];

    const wave =
      0.5 +
      0.5 *
        Math.sin(
          elapsedTime * data.speed +
            data.pulse +
            Math.sin(elapsedTime * 0.18) * 0.8,
        );

    const brightness = 0.88 + wave * data.strength;
    const animated = base.clone().multiplyScalar(brightness);

    const vertexStart = face * 9;

    for (let i = 0; i < 3; i++) {
      const offset = vertexStart + i * 3;
      colorArray[offset] = animated.r;
      colorArray[offset + 1] = animated.g;
      colorArray[offset + 2] = animated.b;
    }
  }

  colors.needsUpdate = true;

  // Breathing without deforming the O shape.
  const breath = 1 + Math.sin(elapsedTime * 1.15) * 0.006;
  orbitO.mesh.scale.set(1.12 * breath, 1 * breath, 1);

  orbitO.mesh.rotation.z = Math.sin(elapsedTime * 0.18) * 0.025;
  orbitO.material.emissiveIntensity = 0.2 + Math.sin(elapsedTime * 0.9) * 0.035;
}

function weightedColorIndex() {
  const r = Math.random();

  if (r < 0.42) return 0;
  if (r < 0.72) return 1;
  if (r < 0.9) return 2;
  if (r < 0.975) return 3;

  return 4;
}

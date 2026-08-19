import * as THREE from "three";

/* =========================================================
   ORBIT STORE — LIVING FACET O
   ========================================================= */

/* =========================================================
   DOM
   ========================================================= */

const canvas = document.getElementById("webgl");
const startup = document.querySelector(".startup");
const tagline = document.querySelector(".tagline");
const enterOrbitButton = document.getElementById("enterOrbit");

if (!canvas) {
  throw new Error("Orbit WebGL canvas #webgl was not found.");
}

/* =========================================================
   WEBGL CHECK
   ========================================================= */

function canUseWebGL() {
  try {
    const testCanvas = document.createElement("canvas");

    const context =
      testCanvas.getContext("webgl2") ||
      testCanvas.getContext("webgl") ||
      testCanvas.getContext("experimental-webgl");

    return Boolean(context);
  } catch {
    return false;
  }
}

if (!canUseWebGL()) {
  document.body.classList.add("webgl-unavailable");

  if (startup) {
    startup.style.visibility = "visible";
    startup.style.opacity = "1";
  }

  document.body.classList.add("orbit-tagline-visible");

  document.body.classList.add("orbit-controls-visible");

  if (typeof window.completeOrbitLoader === "function") {
    window.completeOrbitLoader();
  }

  throw new Error("WebGL is unavailable.");
}

/* =========================================================
   QUALITY
   ========================================================= */

function getQualityLevel() {
  const width = window.innerWidth;
  const height = window.innerHeight;

  const smallest = Math.min(width, height);

  const memory =
    typeof navigator.deviceMemory === "number" ? navigator.deviceMemory : 4;

  const cores =
    typeof navigator.hardwareConcurrency === "number"
      ? navigator.hardwareConcurrency
      : 4;

  const touch = "ontouchstart" in window || navigator.maxTouchPoints > 0;

  if (
    smallest < 550 ||
    (touch && smallest < 800) ||
    memory <= 2 ||
    cores <= 2
  ) {
    return "low";
  }

  if (smallest < 900 || memory <= 4 || cores <= 4) {
    return "medium";
  }

  return "high";
}

let quality = getQualityLevel();

/* =========================================================
   SCENE
   ========================================================= */

const scene = new THREE.Scene();

scene.background = new THREE.Color(0x020307);

/* =========================================================
   CAMERA
   ========================================================= */

const camera = new THREE.PerspectiveCamera(
  32,
  window.innerWidth / window.innerHeight,
  0.1,
  100,
);

camera.position.set(0, 0.05, 8);

/* =========================================================
   RENDERER
   ========================================================= */

let renderer;

try {
  renderer = new THREE.WebGLRenderer({
    canvas,
    antialias: quality !== "low",
    alpha: true,
    powerPreference: "high-performance",
  });
} catch (error) {
  document.body.classList.add("webgl-unavailable");

  console.error("Orbit Store WebGL initialization failed.", error);

  throw error;
}

renderer.setPixelRatio(
  Math.min(window.devicePixelRatio || 1, quality === "low" ? 1.35 : 2),
);

renderer.setSize(window.innerWidth, window.innerHeight, false);

renderer.outputColorSpace = THREE.SRGBColorSpace;

renderer.toneMapping = THREE.ACESFilmicToneMapping;

renderer.toneMappingExposure = 1.04;

/* =========================================================
   LIGHTING
   ========================================================= */

const ambientLight = new THREE.AmbientLight(0x718594, 0.44);

scene.add(ambientLight);

const keyLight = new THREE.DirectionalLight(0xffffff, 3.7);

keyLight.position.set(-4, 4.8, 6);

scene.add(keyLight);

const secondaryLight = new THREE.DirectionalLight(0xd6e4ed, 1.35);

secondaryLight.position.set(4, -1.5, 4);

scene.add(secondaryLight);

const rimLight = new THREE.PointLight(0x1674ff, 21, 17);

rimLight.position.set(3.8, 0.6, 3.8);

scene.add(rimLight);

const cyanLight = new THREE.PointLight(0x36c6d8, 7, 13);

cyanLight.position.set(-3.5, 1.5, 2.5);

scene.add(cyanLight);

const fillLight = new THREE.PointLight(0x4b6c7d, 4.6, 15);

fillLight.position.set(-4, -3, 2);

scene.add(fillLight);

/* =========================================================
   MAIN ORBIT GROUP
   ========================================================= */

const orbitGroup = new THREE.Group();

orbitGroup.position.y = 0.47;

scene.add(orbitGroup);

/* =========================================================
   RESPONSIVE SCALE
   ========================================================= */

function getOrbitScale() {
  const smallest = Math.min(window.innerWidth, window.innerHeight);

  if (smallest >= 1400) return 1.24;

  if (smallest >= 1000) return 1.16;

  if (smallest >= 750) return 1.02;

  if (smallest >= 550) return 0.88;

  return 0.7;
}

orbitGroup.scale.setScalar(getOrbitScale());

/* =========================================================
   O GEOMETRY
   ========================================================= */

const sourceGeometry = new THREE.TorusGeometry(
  1.42,
  0.205,
  quality === "low" ? 14 : 20,
  quality === "low" ? 72 : 112,
);

const oGeometry = sourceGeometry.toNonIndexed();

sourceGeometry.dispose();

oGeometry.computeVertexNormals();

const positionAttribute = oGeometry.getAttribute("position");

const normalAttribute = oGeometry.getAttribute("normal");

const vertexCount = positionAttribute.count;

const basePositions = new Float32Array(positionAttribute.array);

const startPositions = new Float32Array(vertexCount * 3);

const colors = new Float32Array(vertexCount * 3);

/* =========================================================
   COLORS
   ========================================================= */

const baseColor = new THREE.Color(0x6f8392);

const highlightColor = new THREE.Color(0xdceaf4);

const blueColor = new THREE.Color(0x417ea6);

const cyanColor = new THREE.Color(0x45b7c8);

const shadowColor = new THREE.Color(0x182934);

const tempColor = new THREE.Color();

const tempColor2 = new THREE.Color();

/* =========================================================
   FACET DATA

   Every TRIANGLE gets its own personality.
   ========================================================= */

const facetData = [];

/* ---------------------------------------------------------
   Determine center of each triangle
   --------------------------------------------------------- */

for (let i = 0; i < vertexCount; i += 3) {
  const centerX =
    (basePositions[i * 3] +
      basePositions[(i + 1) * 3] +
      basePositions[(i + 2) * 3]) /
    3;

  const centerY =
    (basePositions[i * 3 + 1] +
      basePositions[(i + 1) * 3 + 1] +
      basePositions[(i + 2) * 3 + 1]) /
    3;

  const centerZ =
    (basePositions[i * 3 + 2] +
      basePositions[(i + 1) * 3 + 2] +
      basePositions[(i + 2) * 3 + 2]) /
    3;

  /*
   * Angular position around the O.
   *
   * This is extremely important because
   * the waves will travel around the ring.
   */

  const angle = Math.atan2(centerY, centerX);

  /*
   * Radial distance.
   */

  const radialDistance = Math.sqrt(centerX * centerX + centerY * centerY);

  /*
   * Individual base material color.
   */

  const random = Math.random();

  let faceColor;

  if (random > 0.92) {
    faceColor = highlightColor.clone();

    faceColor.multiplyScalar(0.78 + Math.random() * 0.22);
  } else if (random < 0.1) {
    faceColor = blueColor.clone();

    faceColor.multiplyScalar(0.65 + Math.random() * 0.3);
  } else {
    faceColor = baseColor.clone();

    faceColor.multiplyScalar(0.82 + Math.random() * 0.22);
  }

  /* -------------------------------------------------------
     Starting position outside the final O
     ------------------------------------------------------- */

  const edgeSide = Math.floor(Math.random() * 4);

  let startX = centerX;
  let startY = centerY;

  if (edgeSide === 0) {
    startX = -5.7 - Math.random() * 2;

    startY = (Math.random() - 0.5) * 5;
  } else if (edgeSide === 1) {
    startX = 5.7 + Math.random() * 2;

    startY = (Math.random() - 0.5) * 5;
  } else if (edgeSide === 2) {
    startX = (Math.random() - 0.5) * 7.8;

    startY = 3.6 + Math.random() * 1.8;
  } else {
    startX = (Math.random() - 0.5) * 7.8;

    startY = -3.6 - Math.random() * 1.8;
  }

  const startZ = centerZ + 1.4 + Math.random() * 3.1;

  /* -------------------------------------------------------
     Store personality
     ------------------------------------------------------- */

  facetData.push({
    base: faceColor.clone(),

    centerX,
    centerY,
    centerZ,

    angle,

    radialDistance,

    /*
     * Primary breathing.
     */
    phase: Math.random() * Math.PI * 2,

    speed: 0.7 + Math.random() * 1.2,

    breathe: 0.035 + Math.random() * 0.025,

    /*
     * Secondary breathing.
     */
    secondaryPhase: Math.random() * Math.PI * 2,

    secondarySpeed: 0.35 + Math.random() * 0.45,

    /*
     * Traveling wave phase.
     */
    waveOffset: Math.random() * Math.PI * 2,

    /*
     * Individual pulse.
     */
    pulsePhase: Math.random() * Math.PI * 2,

    pulseSpeed: 0.8 + Math.random() * 1.5,

    pulseStrength: 0.012 + Math.random() * 0.018,

    /*
     * Shadow variation.
     */
    shadePhase: Math.random() * Math.PI * 2,

    shadeStrength: 0.18 + Math.random() * 0.18,

    /*
     * Small physical randomness.
     */
    organicPhase: Math.random() * Math.PI * 2,

    organicStrength: 0.006 + Math.random() * 0.009,

    /*
     * Interaction.
     */
    interactionX: 0,
    interactionY: 0,
    interactionZ: 0,

    interactionVelocityX: 0,
    interactionVelocityY: 0,
    interactionVelocityZ: 0,

    interactionSeed: Math.random() * Math.PI * 2,

    /*
     * Intro.
     */
    settleDelay: Math.random() * 0.75,

    settleSpeed: 0.68 + Math.random() * 0.2,

    /*
     * Starting coordinates.
     */
    startX,
    startY,
    startZ,
  });

  /* -------------------------------------------------------
     Initialize vertices
     ------------------------------------------------------- */

  for (let v = 0; v < 3; v++) {
    const vertex = i + v;

    const i3 = vertex * 3;

    const offsetX = basePositions[i3] - centerX;

    const offsetY = basePositions[i3 + 1] - centerY;

    const offsetZ = basePositions[i3 + 2] - centerZ;

    startPositions[i3] = startX + offsetX * 0.25;

    startPositions[i3 + 1] = startY + offsetY * 0.25;

    startPositions[i3 + 2] = startZ + offsetZ * 0.25;

    positionAttribute.array[i3] = startPositions[i3];

    positionAttribute.array[i3 + 1] = startPositions[i3 + 1];

    positionAttribute.array[i3 + 2] = startPositions[i3 + 2];

    colors[i3] = faceColor.r * 0.12;

    colors[i3 + 1] = faceColor.g * 0.12;

    colors[i3 + 2] = faceColor.b * 0.12;
  }
}

oGeometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));

positionAttribute.setUsage(THREE.DynamicDrawUsage);

oGeometry.getAttribute("color").setUsage(THREE.DynamicDrawUsage);

/* =========================================================
   O MATERIAL
   ========================================================= */

const oMaterial = new THREE.MeshPhysicalMaterial({
  vertexColors: true,

  metalness: 0.88,

  roughness: 0.24,

  clearcoat: 0.82,

  clearcoatRoughness: 0.17,

  emissive: new THREE.Color(0x0b1721),

  emissiveIntensity: 0.44,

  flatShading: true,
});

const orbitO = new THREE.Mesh(oGeometry, oMaterial);

orbitO.scale.set(1.13, 1, 1);

orbitGroup.add(orbitO);

/* =========================================================
   CURSOR
   ========================================================= */

const pointer = new THREE.Vector2(0, 0);

const pointerTarget = new THREE.Vector2(0, 0);

const pointerWorld = new THREE.Vector3();

const pointerLocal = new THREE.Vector3();

/*
 * Last known pointer position in local space.
 * Used to drive distance-based return sequencing
 * when the pointer leaves the scene.
 */
const lastPointerLocal = new THREE.Vector3();

let lastPointerLocalValid = false;

const pointerRaycaster = new THREE.Raycaster();

const interactionPlane = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0);

let pointerActive = false;

/* =========================================================
   MOUSE ROTATION
   ========================================================= */

const targetRotation = new THREE.Vector2(0, 0);

const currentRotation = new THREE.Vector2(0, 0);

window.addEventListener(
  "pointermove",
  (event) => {
    if (orbitEnterState.requested) {
      return;
    }

    pointerActive = true;

    const normalizedX = (event.clientX / window.innerWidth) * 2 - 1;

    const normalizedY = -(event.clientY / window.innerHeight) * 2 + 1;

    pointerTarget.x = normalizedX;

    pointerTarget.y = normalizedY;

    targetRotation.x = normalizedY * 0.12;

    targetRotation.y = normalizedX * 0.18;
  },
  {
    passive: true,
  },
);

window.addEventListener("pointerleave", () => {
  pointerActive = false;

  pointerTarget.set(0, 0);

  targetRotation.set(0, 0);
});

window.addEventListener("blur", () => {
  pointerActive = false;

  pointerTarget.set(0, 0);

  targetRotation.set(0, 0);
});

/* =========================================================
   PARTICLE COUNT
   ========================================================= */

function getParticleCount() {
  const smallest = Math.min(window.innerWidth, window.innerHeight);

  if (quality === "low") {
    return 120;
  }

  if (smallest >= 1400) {
    return 850;
  }

  if (smallest >= 1000) {
    return 680;
  }

  if (smallest >= 750) {
    return 500;
  }

  if (smallest >= 550) {
    return 330;
  }

  return 190;
}

/* =========================================================
   NATURAL PARTICLES
   ========================================================= */

function createNaturalParticleField() {
  const count = getParticleCount();

  const positions = new Float32Array(count * 3);

  const brightness = new Float32Array(count);

  for (let i = 0; i < count; i++) {
    const i3 = i * 3;

    const theta = Math.random() * Math.PI * 2;

    const phi = Math.acos(2 * Math.random() - 1);

    const radius = 1.8 + Math.pow(Math.random(), 0.62) * 3;

    positions[i3] = Math.sin(phi) * Math.cos(theta) * radius;

    positions[i3 + 1] = Math.cos(phi) * radius * 0.72;

    positions[i3 + 2] = Math.sin(phi) * Math.sin(theta) * radius * 0.68;

    const roll = Math.random();

    if (roll < 0.58) {
      brightness[i] = 0.18 + Math.random() * 0.18;
    } else if (roll < 0.88) {
      brightness[i] = 0.4 + Math.random() * 0.24;
    } else if (roll < 0.975) {
      brightness[i] = 0.68 + Math.random() * 0.22;
    } else {
      brightness[i] = 0.92 + Math.random() * 0.08;
    }
  }

  return {
    positions,
    brightness,
    count,
  };
}

const particleData = createNaturalParticleField();

const particleColors = new Float32Array(particleData.count * 3);

const white = new THREE.Color(0xdceaf2);

const coolWhite = new THREE.Color(0xf0f5f8);

const blue = new THREE.Color(0x5a9ec5);

const cyan = new THREE.Color(0x45b7c8);

for (let i = 0; i < particleData.count; i++) {
  const i3 = i * 3;

  const roll = Math.random();

  let particleColor;

  if (roll < 0.68) {
    particleColor = white;
  } else if (roll < 0.91) {
    particleColor = coolWhite;
  } else if (roll < 0.975) {
    particleColor = blue;
  } else {
    particleColor = cyan;
  }

  const b = particleData.brightness[i];

  particleColors[i3] = particleColor.r * b;

  particleColors[i3 + 1] = particleColor.g * b;

  particleColors[i3 + 2] = particleColor.b * b;
}

const particleGeometry = new THREE.BufferGeometry();

particleGeometry.setAttribute(
  "position",
  new THREE.BufferAttribute(particleData.positions, 3),
);

particleGeometry.setAttribute(
  "color",
  new THREE.BufferAttribute(particleColors, 3),
);

const particleMaterial = new THREE.PointsMaterial({
  size: quality === "low" ? 0.017 : 0.022,

  vertexColors: true,

  transparent: true,

  opacity: 0.88,

  depthWrite: false,

  blending: THREE.AdditiveBlending,
});

const particles = new THREE.Points(particleGeometry, particleMaterial);

orbitGroup.add(particles);

/* =========================================================
   CROSS PARTICLES
   ========================================================= */

function createCrossParticles() {
  const smallest = Math.min(window.innerWidth, window.innerHeight);

  let count;

  if (quality === "low") {
    count = 40;
  } else if (smallest >= 1400) {
    count = 220;
  } else if (smallest >= 1000) {
    count = 175;
  } else if (smallest >= 750) {
    count = 135;
  } else if (smallest >= 550) {
    count = 95;
  } else {
    count = 60;
  }

  const positions = new Float32Array(count * 3);

  const colors = new Float32Array(count * 3);

  for (let i = 0; i < count; i++) {
    const i3 = i * 3;

    const diagonal = i % 2 === 0 ? 1 : -1;

    const along = (Math.random() - 0.5) * 4.7;

    const thickness = (Math.random() - 0.5) * 0.28;

    positions[i3] = along;

    positions[i3 + 1] = diagonal * along * 0.43 + thickness;

    positions[i3 + 2] = -0.35 - Math.random() * 1.25;

    const brightness = 0.16 + Math.random() * 0.55;

    const color = Math.random() > 0.9 ? blue : white;

    colors[i3] = color.r * brightness;

    colors[i3 + 1] = color.g * brightness;

    colors[i3 + 2] = color.b * brightness;
  }

  const geometry = new THREE.BufferGeometry();

  geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3));

  geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));

  const material = new THREE.PointsMaterial({
    size: quality === "low" ? 0.014 : 0.018,

    vertexColors: true,

    transparent: true,

    opacity: 0.7,

    depthWrite: false,

    blending: THREE.AdditiveBlending,
  });

  return new THREE.Points(geometry, material);
}

const crossParticles = createCrossParticles();

orbitGroup.add(crossParticles);

/* =========================================================
   MICRO PARTICLES
   ========================================================= */

function createMicroParticles() {
  const count = Math.round(getParticleCount() * 0.42);

  const positions = new Float32Array(count * 3);

  const colors = new Float32Array(count * 3);

  for (let i = 0; i < count; i++) {
    const i3 = i * 3;

    positions[i3] = (Math.random() - 0.5) * 10;

    positions[i3 + 1] = (Math.random() - 0.5) * 6.5;

    positions[i3 + 2] = (Math.random() - 0.5) * 5;

    const brightness = 0.12 + Math.random() * 0.3;

    const color = Math.random() > 0.9 ? blue : white;

    colors[i3] = color.r * brightness;

    colors[i3 + 1] = color.g * brightness;

    colors[i3 + 2] = color.b * brightness;
  }

  const geometry = new THREE.BufferGeometry();

  geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3));

  geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));

  const material = new THREE.PointsMaterial({
    size: quality === "low" ? 0.008 : 0.011,

    vertexColors: true,

    transparent: true,

    opacity: 0.7,

    depthWrite: false,

    blending: THREE.AdditiveBlending,
  });

  return new THREE.Points(geometry, material);
}

const microParticles = createMicroParticles();

scene.add(microParticles);

/* =========================================================
   O SURFACE PARTICLES
   ========================================================= */

function createSurfaceParticles() {
  const count =
    quality === "low"
      ? 35
      : window.innerWidth < 600
        ? 70
        : window.innerWidth < 1000
          ? 110
          : 160;

  const positions = new Float32Array(count * 3);

  const colors = new Float32Array(count * 3);

  for (let i = 0; i < count; i++) {
    const i3 = i * 3;

    const angle = Math.random() * Math.PI * 2;

    const tubeAngle = Math.random() * Math.PI * 2;

    const radius = 1.42 + 0.205 * Math.cos(tubeAngle);

    positions[i3] = radius * Math.cos(angle) * 1.13;

    positions[i3 + 1] = 0.205 * Math.sin(tubeAngle);

    positions[i3 + 2] = radius * Math.sin(angle);

    const color = Math.random() > 0.84 ? blue : white;

    const brightness = 0.35 + Math.random() * 0.6;

    colors[i3] = color.r * brightness;

    colors[i3 + 1] = color.g * brightness;

    colors[i3 + 2] = color.b * brightness;
  }

  const geometry = new THREE.BufferGeometry();

  geometry.setAttribute("position", new THREE.BufferAttribute(positions, 3));

  geometry.setAttribute("color", new THREE.BufferAttribute(colors, 3));

  const material = new THREE.PointsMaterial({
    size: quality === "low" ? 0.013 : 0.018,

    vertexColors: true,

    transparent: true,

    opacity: 0.68,

    depthWrite: false,

    blending: THREE.AdditiveBlending,
  });

  return new THREE.Points(geometry, material);
}

const surfaceParticles = createSurfaceParticles();

orbitGroup.add(surfaceParticles);

/* =========================================================
   ORBITAL CURVES
   ========================================================= */

const orbitalCurves = [];

function createOrbitalCurve(
  radiusX,
  radiusY,
  rotationX,
  rotationY,
  rotationZ,
  phase,
  opacity,
) {
  const points = [];

  const segments = quality === "low" ? 48 : 80;

  for (let i = 0; i <= segments; i++) {
    const t = (i / segments) * Math.PI * 2;

    const x = Math.cos(t) * radiusX;

    const y = Math.sin(t + phase) * radiusY;

    const z = Math.sin(t * 2 + phase) * 0.18;

    points.push(new THREE.Vector3(x, y, z));
  }

  const curve = new THREE.CatmullRomCurve3(points, true, "centripetal");

  const geometry = new THREE.TubeGeometry(
    curve,
    segments,
    quality === "low" ? 0.004 : 0.006,
    5,
    true,
  );

  const material = new THREE.MeshBasicMaterial({
    color: opacity > 0.1 ? 0x4b9fc0 : 0x7a95a6,

    transparent: true,

    opacity,

    depthWrite: false,

    blending: THREE.AdditiveBlending,
  });

  const mesh = new THREE.Mesh(geometry, material);

  mesh.rotation.set(rotationX, rotationY, rotationZ);

  orbitGroup.add(mesh);

  orbitalCurves.push({
    mesh,
    baseRotationX: rotationX,
    baseRotationY: rotationY,
    baseRotationZ: rotationZ,
    phase,
  });

  return mesh;
}

if (quality !== "low") {
  createOrbitalCurve(2.55, 1.1, 0.58, -0.22, 0.1, 0, 0.075);

  createOrbitalCurve(2.8, 0.82, -0.38, 0.4, -0.2, 1.9, 0.045);

  if (quality === "high") {
    createOrbitalCurve(3.25, 1.38, 0.12, -0.5, 0.35, 3.4, 0.028);
  }
}

/* =========================================================
   FLOATING FRAGMENTS
   ========================================================= */

const fragments = [];

function createFloatingFragments() {
  if (quality === "low") {
    return;
  }

  const count = quality === "high" ? 12 : 7;

  const geometry = new THREE.IcosahedronGeometry(0.035, 0);

  for (let i = 0; i < count; i++) {
    const material = new THREE.MeshPhysicalMaterial({
      color: Math.random() > 0.18 ? 0x9db5c3 : 0x397b9d,

      metalness: 0.68,

      roughness: 0.24,

      clearcoat: 0.75,

      clearcoatRoughness: 0.2,

      transparent: true,

      opacity: 0.25 + Math.random() * 0.3,

      emissive: new THREE.Color(0x102c3b),

      emissiveIntensity: 0.24,
    });

    const fragment = new THREE.Mesh(geometry, material);

    const angle = Math.random() * Math.PI * 2;

    const radius = 2.5 + Math.random() * 2.3;

    fragment.position.set(
      Math.cos(angle) * radius,

      -0.7 + Math.random() * 2.1,

      -0.3 + Math.random() * 1.8,
    );

    const scale = 0.65 + Math.random() * 1.8;

    fragment.scale.set(scale, scale * (0.7 + Math.random() * 0.7), scale);

    fragment.rotation.set(
      Math.random() * Math.PI,

      Math.random() * Math.PI,

      Math.random() * Math.PI,
    );

    orbitGroup.add(fragment);

    fragments.push({
      mesh: fragment,

      phase: Math.random() * Math.PI * 2,

      orbitRadius: radius,

      speed: 0.05 + Math.random() * 0.08,

      floatSpeed: 0.2 + Math.random() * 0.25,

      floatAmount: 0.035 + Math.random() * 0.08,

      baseX: fragment.position.x,

      baseY: fragment.position.y,

      baseZ: fragment.position.z,
    });
  }
}

createFloatingFragments();

/* =========================================================
   REFLECTION
   ========================================================= */

function createReflectionTexture() {
  const size = 256;

  const reflectionCanvas = document.createElement("canvas");

  reflectionCanvas.width = size;

  reflectionCanvas.height = size;

  const context = reflectionCanvas.getContext("2d");

  const gradient = context.createRadialGradient(
    size / 2,
    size / 2,
    0,
    size / 2,
    size / 2,
    size / 2,
  );

  gradient.addColorStop(0, "rgba(55,145,176,0.15)");

  gradient.addColorStop(0.2, "rgba(35,104,132,0.10)");

  gradient.addColorStop(0.55, "rgba(15,52,70,0.045)");

  gradient.addColorStop(1, "rgba(0,0,0,0)");

  context.fillStyle = gradient;

  context.fillRect(0, 0, size, size);

  return new THREE.CanvasTexture(reflectionCanvas);
}

const reflectionTexture = createReflectionTexture();

const reflectionGeometry = new THREE.PlaneGeometry(7, 3.2);

const reflectionMaterial = new THREE.MeshBasicMaterial({
  map: reflectionTexture,

  transparent: true,

  opacity: quality === "high" ? 0.75 : 0.48,

  depthWrite: false,

  blending: THREE.AdditiveBlending,
});

const reflectionPlane = new THREE.Mesh(reflectionGeometry, reflectionMaterial);

reflectionPlane.rotation.x = -Math.PI / 2;

reflectionPlane.position.set(0, -1.2, 0.3);

scene.add(reflectionPlane);

const reflectionRingGroup = new THREE.Group();

reflectionRingGroup.position.set(0, -1.17, 0.3);

scene.add(reflectionRingGroup);

if (quality !== "low") {
  const ringGeometry = new THREE.TorusGeometry(1.3, 0.006, 5, 96);

  const ringMaterial = new THREE.MeshBasicMaterial({
    color: 0x397f9b,

    transparent: true,

    opacity: 0.045,

    depthWrite: false,

    blending: THREE.AdditiveBlending,
  });

  const reflectionRing = new THREE.Mesh(ringGeometry, ringMaterial);

  reflectionRing.scale.set(1.65, 1, 0.42);

  reflectionRing.rotation.x = -Math.PI / 2;

  reflectionRingGroup.add(reflectionRing);
}

/* =========================================================
   POINTER → LOCAL SPACE
   ========================================================= */

function updatePointerLocal() {
  if (!pointerActive || orbitEnterState.requested) {
    return false;
  }

  pointer.lerp(pointerTarget, 0.12);

  pointerRaycaster.setFromCamera(pointer, camera);

  if (!pointerRaycaster.ray.intersectPlane(interactionPlane, pointerWorld)) {
    return false;
  }

  orbitGroup.updateMatrixWorld(true);

  pointerLocal.copy(pointerWorld);

  orbitGroup.worldToLocal(pointerLocal);

  /*
   * Keep a snapshot of the last valid pointer
   * position so we can sequence the return
   * animation by distance when the cursor leaves.
   */
  lastPointerLocal.copy(pointerLocal);
  lastPointerLocalValid = true;

  return true;
}

/* =========================================================
   INTERACTION
   =========================================================

   Changes vs original:
   - When the pointer is absent, each facet's spring rate
     is modulated by its distance from the last known
     pointer position: closer facets return quickly,
     farther facets return slowly.
   - Damping is raised on the return pass to kill
     the rebound overshoot without reducing displacement.
   ========================================================= */

function updateFacetInteraction(elapsed, interactionEnabled) {
  const hasPointer = interactionEnabled && updatePointerLocal();

  const interactionRadius = quality === "low" ? 0.78 : 1.08;

  const interactionStrength =
    quality === "low" ? 0.22 : quality === "medium" ? 0.32 : 0.4;

  /*
   * Return-pass constants.
   *
   * We keep two spring values:
   *   returnSpringNear  — facets very close to the last cursor position
   *   returnSpringFar   — facets far from the last cursor position
   *
   * The actual per-facet spring is linearly interpolated
   * between these two based on normalized distance.
   *
   * Damping is deliberately high (0.88) so the facets glide
   * to rest without bouncing back.
   */
  const returnSpringNear = 0.14;
  const returnSpringFar = 0.028;
  const returnDamping = 0.88;

  /*
   * The "influence radius" used for sequencing the return.
   * Facets within this radius of the last cursor point are
   * treated as "near"; beyond it they are "far".
   */
  const returnSequenceRadius = 1.6;

  for (let face = 0; face < facetData.length; face++) {
    const facet = facetData[face];

    let desiredX = 0;
    let desiredY = 0;
    let desiredZ = 0;

    if (hasPointer) {
      /* ---------------------------------------------------
         Active interaction — same logic as before.
         --------------------------------------------------- */

      const dx = facet.centerX - pointerLocal.x;

      const dy = facet.centerY - pointerLocal.y;

      const dz = facet.centerZ - pointerLocal.z;

      const distance = Math.sqrt(dx * dx + dy * dy + dz * dz * 0.3);

      if (distance < interactionRadius) {
        const normalized =
          1 - Math.min(Math.max(distance / interactionRadius, 0), 1);

        const falloff = normalized * normalized * (3 - 2 * normalized);

        const planarLength = Math.sqrt(dx * dx + dy * dy);

        let awayX = 0;
        let awayY = 0;

        if (planarLength > 0.0001) {
          awayX = dx / planarLength;

          awayY = dy / planarLength;
        } else {
          awayX = Math.cos(facet.interactionSeed);

          awayY = Math.sin(facet.interactionSeed);
        }

        const ripple =
          Math.sin(elapsed * 2.4 + facet.interactionSeed) * 0.035 * falloff;

        desiredX = awayX * interactionStrength * falloff + ripple;

        desiredY = awayY * interactionStrength * falloff + ripple * 0.65;

        desiredZ = Math.min(0.22, Math.abs(dz) * 0.06) * falloff;

        desiredZ += 0.08 * falloff;
      }

      /*
       * Standard push spring while the pointer is present.
       * Values unchanged from original — displacement is identical.
       */
      const spring = 0.18;
      const damping = 0.78;

      facet.interactionVelocityX += (desiredX - facet.interactionX) * spring;
      facet.interactionVelocityY += (desiredY - facet.interactionY) * spring;
      facet.interactionVelocityZ += (desiredZ - facet.interactionZ) * spring;

      facet.interactionVelocityX *= damping;
      facet.interactionVelocityY *= damping;
      facet.interactionVelocityZ *= damping;
    } else {
      /* ---------------------------------------------------
         Return pass — pointer is gone.

         Compute distance from the last known cursor position
         and derive a per-facet spring rate so that close
         facets rush back first and distant ones drift in
         gradually.
         --------------------------------------------------- */

      let distFromLast = returnSequenceRadius; // default to "far"

      if (lastPointerLocalValid) {
        const dx = facet.centerX - lastPointerLocal.x;
        const dy = facet.centerY - lastPointerLocal.y;
        distFromLast = Math.sqrt(dx * dx + dy * dy);
      }

      /*
       * Normalise within the sequencing radius and clamp.
       * t = 0 → right at the cursor centre (fast return)
       * t = 1 → at or beyond the radius   (slow return)
       */
      const t = Math.min(distFromLast / returnSequenceRadius, 1.0);

      /*
       * Linear blend: near facets get returnSpringNear,
       * far facets get returnSpringFar.
       */
      const spring =
        returnSpringNear + (returnSpringFar - returnSpringNear) * t;

      facet.interactionVelocityX += (0 - facet.interactionX) * spring;
      facet.interactionVelocityY += (0 - facet.interactionY) * spring;
      facet.interactionVelocityZ += (0 - facet.interactionZ) * spring;

      /*
       * High damping kills the overshoot / rebound.
       * The displacement amount stays the same because
       * that is determined by the push phase above.
       */
      facet.interactionVelocityX *= returnDamping;
      facet.interactionVelocityY *= returnDamping;
      facet.interactionVelocityZ *= returnDamping;
    }

    facet.interactionX += facet.interactionVelocityX;
    facet.interactionY += facet.interactionVelocityY;
    facet.interactionZ += facet.interactionVelocityZ;
  }
}

/* =========================================================
   ⭐ LIVING FACET ENGINE ⭐
   =========================================================

   This is the major difference from the previous version.

   Each triangle is affected by:

   1. Primary breathing
   2. Secondary breathing
   3. Circumference wave
   4. Counter-wave
   5. Local pulse
   6. Organic noise
   7. Moving shadow
   8. Moving highlight
   9. Mouse displacement

   Therefore the O NEVER moves as one rigid object.
   ========================================================= */

function updateLivingO(elapsed) {
  if (introStartedAt === null) {
    if (startup && window.getComputedStyle(startup).visibility !== "visible") {
      return;
    }

    introStartedAt = elapsed - 1;
  }

  const introElapsed = Math.max(elapsed - introStartedAt, 0);

  const colorAttribute = oGeometry.getAttribute("color");

  const colorArray = colorAttribute.array;

  let introCompleteCount = 0;

  /* -------------------------------------------------------
     GLOBAL LIVING BREATH

     Very slow global breathing prevents
     the O from ever becoming completely static.
     ------------------------------------------------------- */

  const globalBreath = Math.sin(elapsed * 0.72) * 0.026;

  const globalBreath2 = Math.sin(elapsed * 1.13 + 1.7) * 0.014;

  const globalBreath3 = Math.sin(elapsed * 0.34) * 0.018;

  /* -------------------------------------------------------
     WAVE SPEED
     ------------------------------------------------------- */

  const waveTime = elapsed * 1.15;

  const shadowTime = elapsed * 0.74;

  for (let face = 0; face < facetData.length; face++) {
    const facet = facetData[face];

    /* -----------------------------------------------------
       INTRO
       ----------------------------------------------------- */

    const localIntro = Math.min(
      Math.max((introElapsed - facet.settleDelay) * facet.settleSpeed, 0),
      1,
    );

    const introProgress = 1 - Math.pow(1 - localIntro, 3);

    if (localIntro >= 1) {
      introCompleteCount++;
    }

    /* =====================================================
       1. PRIMARY BREATHING
       ===================================================== */

    const primaryBreath =
      Math.sin(elapsed * facet.speed + facet.phase) * facet.breathe;

    /* =====================================================
       2. SECONDARY BREATHING
       ===================================================== */

    const secondaryBreath =
      Math.sin(elapsed * facet.secondarySpeed + facet.secondaryPhase) * 0.018;

    /* =====================================================
       3. CIRCULAR TRAVELING WAVE

       The wave travels around the O.
       ===================================================== */

    const circularWave = Math.sin(
      facet.angle * 5.2 - waveTime + facet.waveOffset,
    );

    const circularWave2 = Math.sin(
      facet.angle * 9.0 + waveTime * 0.72 + facet.waveOffset,
    );

    /* =====================================================
       4. VERTICAL / RADIAL WAVE
       ===================================================== */

    const radialWave = Math.sin(
      facet.radialDistance * 10.0 - elapsed * 1.35 + facet.phase,
    );

    /* =====================================================
       5. LOCAL PULSE

       Gives individual triangles their own
       little "heartbeat".
       ===================================================== */

    const pulse =
      Math.pow(
        Math.max(0, Math.sin(elapsed * facet.pulseSpeed + facet.pulsePhase)),
        3,
      ) * facet.pulseStrength;

    /* =====================================================
       6. ORGANIC MICRO MOTION
       ===================================================== */

    const organic =
      Math.sin(elapsed * 1.7 + facet.organicPhase) * facet.organicStrength;

    /* =====================================================
       7. COMBINE DEPTH MOTION
       ===================================================== */

    const waveDepth =
      circularWave * 0.036 + circularWave2 * 0.018 + radialWave * 0.014;

    /*
     * This is the main "breathing" value.
     *
     * It is deliberately much stronger than
     * the previous version.
     */

    const breathing =
      globalBreath +
      globalBreath2 +
      globalBreath3 +
      primaryBreath +
      secondaryBreath +
      waveDepth +
      pulse +
      organic;

    /* =====================================================
       8. SECONDARY SURFACE WAVES
       ===================================================== */

    const surfaceWave = Math.sin(
      facet.angle * 3.0 + elapsed * 0.92 + facet.secondaryPhase,
    );

    const surfaceWave2 = Math.sin(
      facet.angle * 7.0 - elapsed * 1.48 + facet.phase,
    );

    const surfaceOffset = surfaceWave * 0.016 + surfaceWave2 * 0.009;

    /* =====================================================
       9. SHADOW / HIGHLIGHT WAVE

       Instead of simply changing brightness
       randomly, a moving band sweeps across
       the O.

       This creates the feeling that the facets
       are continuously changing their orientation
       relative to a moving light source.
       ===================================================== */

    const lightBand = Math.sin(facet.angle * 4.0 - shadowTime * 2.0);

    const secondaryLightBand = Math.sin(
      facet.angle * 8.0 + shadowTime * 1.1 + facet.shadePhase,
    );

    const shade =
      (lightBand * 0.5 + secondaryLightBand * 0.25) * facet.shadeStrength;

    /* =====================================================
       10. BASE BRIGHTNESS
       ===================================================== */

    let brightness = 0.92 + shade + circularWave * 0.08 + pulse * 2.2;

    /*
     * Don't allow a triangle to become
     * completely black.
     */

    brightness = Math.max(0.48, Math.min(1.48, brightness));

    /* =====================================================
       11. OCCASIONAL BLUE/CYAN ENERGY
       ===================================================== */

    const blueEnergy = Math.max(
      0,
      Math.sin(facet.angle * 3.0 - elapsed * 0.62 + facet.phase),
    );

    /*
     * Mostly metallic silver.
     * Only certain moving facets receive
     * blue/cyan influence.
     */

    tempColor.copy(facet.base).multiplyScalar(brightness);

    if (blueEnergy > 0.65) {
      tempColor.lerp(blueColor, (blueEnergy - 0.65) * 0.42);
    }

    /* =====================================================
       12. DEEP SHADOWS

       When the wave moves through a low point,
       slightly darken the triangle.
       ===================================================== */

    if (shade < -0.12) {
      const shadowAmount = Math.min(0.42, -shade * 0.65);

      tempColor2.copy(shadowColor).lerp(tempColor, 1 - shadowAmount);

      tempColor.copy(tempColor2);
    }

    /* =====================================================
       13. INTRO SETTLING RIPPLE
       ===================================================== */

    const introRipple =
      localIntro < 1 ? Math.sin(localIntro * Math.PI) * 0.18 : 0;

    const finalDepth = breathing + introRipple + surfaceOffset;

    /* =====================================================
       14. APPLY TO EVERY VERTEX OF TRIANGLE
       ===================================================== */

    const vertexStart = face * 3;

    for (let v = 0; v < 3; v++) {
      const vertex = vertexStart + v;

      const i3 = vertex * 3;

      /* ---------------------------------------------------
         Local vertex offset from face center.
         --------------------------------------------------- */

      const localX = basePositions[i3] - facet.centerX;

      const localY = basePositions[i3 + 1] - facet.centerY;

      const localZ = basePositions[i3 + 2] - facet.centerZ;

      /*
       * Slightly amplify differences between
       * vertices of the same triangle.

       * This makes the triangle itself deform,
       * rather than simply translating as one unit.
       */

      const vertexFactor = 0.82 + Math.sin(v * 2.7 + facet.phase) * 0.12;

      const vertexBreath = finalDepth * vertexFactor;

      /* ---------------------------------------------------
         Normal displacement
         --------------------------------------------------- */

      const targetX =
        basePositions[i3] + normalAttribute.array[i3] * vertexBreath;

      const targetY =
        basePositions[i3 + 1] + normalAttribute.array[i3 + 1] * vertexBreath;

      const targetZ =
        basePositions[i3 + 2] + normalAttribute.array[i3 + 2] * vertexBreath;

      /* ---------------------------------------------------
         Slight tangential deformation.

         This makes the O feel soft/alive rather than
         just moving in and out.
         --------------------------------------------------- */

      const tangential = surfaceWave * 0.008;

      const deformedX = targetX + localX * tangential;

      const deformedY = targetY + localY * tangential;

      const deformedZ = targetZ + localZ * tangential;

      /* ---------------------------------------------------
         Mouse interaction
         --------------------------------------------------- */

      const interactionX = facet.interactionX * introProgress;

      const interactionY = facet.interactionY * introProgress;

      const interactionZ = facet.interactionZ * introProgress;

      const finalX = deformedX + interactionX;

      const finalY = deformedY + interactionY;

      const finalZ = deformedZ + interactionZ;

      /* ---------------------------------------------------
         Intro interpolation
         --------------------------------------------------- */

      positionAttribute.array[i3] =
        startPositions[i3] + (finalX - startPositions[i3]) * introProgress;

      positionAttribute.array[i3 + 1] =
        startPositions[i3 + 1] +
        (finalY - startPositions[i3 + 1]) * introProgress;

      positionAttribute.array[i3 + 2] =
        startPositions[i3 + 2] +
        (finalZ - startPositions[i3 + 2]) * introProgress;

      /* ---------------------------------------------------
         Color
         --------------------------------------------------- */

      colorArray[i3] = tempColor.r;

      colorArray[i3 + 1] = tempColor.g;

      colorArray[i3 + 2] = tempColor.b;
    }
  }

  positionAttribute.needsUpdate = true;

  colorAttribute.needsUpdate = true;

  /*
   * Recalculate normals so the lighting
   * follows the breathing/deformation.
   *
   * This is what makes the moving facets
   * actually cast changing highlights/shadows.
   */

  oGeometry.computeVertexNormals();

  /* =======================================================
     WHOLE-O BREATH

     Very subtle so individual triangles remain
     the dominant motion.
     ======================================================= */

  const wholeBreath =
    1 +
    Math.sin(elapsed * 0.68) * 0.008 +
    Math.sin(elapsed * 1.15 + 1.8) * 0.004;

  orbitO.scale.set(1.13 * wholeBreath, wholeBreath, 1);

  /* =======================================================
     MATERIAL BREATHING
     ======================================================= */

  oMaterial.emissiveIntensity =
    0.42 + Math.sin(elapsed * 0.65) * 0.035 + Math.sin(elapsed * 1.2) * 0.018;

  /* =======================================================
     INTRO COMPLETE
     ======================================================= */

  if (!uiRevealed && introCompleteCount === facetData.length) {
    uiRevealed = true;

    window.setTimeout(() => {
      if (tagline) {
        document.body.classList.add("orbit-tagline-visible");

        scrambleTaglineIn();
      }
    }, 200);

    window.setTimeout(() => {
      document.body.classList.add("orbit-controls-visible");
    }, 2600);
  }
}

/* =========================================================
   ORBITAL ELEMENT ANIMATION
   ========================================================= */

function updateOrbitalElements(elapsed) {
  orbitalCurves.forEach((entry, index) => {
    const mesh = entry.mesh;

    mesh.rotation.x =
      entry.baseRotationX + Math.sin(elapsed * 0.08 + entry.phase) * 0.035;

    mesh.rotation.y =
      entry.baseRotationY + Math.cos(elapsed * 0.07 + entry.phase) * 0.045;

    mesh.rotation.z =
      entry.baseRotationZ + elapsed * (index % 2 === 0 ? 0.008 : -0.006);
  });
}

/* =========================================================
   FRAGMENT ANIMATION
   ========================================================= */

function updateFragments(elapsed) {
  fragments.forEach((fragment) => {
    const mesh = fragment.mesh;

    const orbitAngle = elapsed * fragment.speed + fragment.phase;

    mesh.position.x = fragment.baseX + Math.cos(orbitAngle) * 0.16;

    mesh.position.y =
      fragment.baseY +
      Math.sin(elapsed * fragment.floatSpeed + fragment.phase) *
        fragment.floatAmount;

    mesh.position.z = fragment.baseZ + Math.sin(orbitAngle * 0.72) * 0.12;

    mesh.rotation.x += 0.0015;

    mesh.rotation.y += 0.002;

    mesh.rotation.z += 0.001;
  });
}

/* =========================================================
   TEXT SCRAMBLE
   ========================================================= */

const taglineHTML = tagline ? tagline.innerHTML : "";

let scrambleTimer = null;

let taglineScrambled = false;

function scrambleTextElement(element, finalText, duration, onDone) {
  if (!element) {
    return;
  }

  const glyphs = "01<>/\\[]{}";

  const frameTime = 54;

  const frames = Math.max(1, Math.round(duration / frameTime));

  let frame = 0;

  if (scrambleTimer) {
    window.clearInterval(scrambleTimer);

    scrambleTimer = null;
  }

  element.classList.add("is-scrambling");

  scrambleTimer = window.setInterval(() => {
    const progress = frame / frames;

    element.textContent = finalText
      .split("")
      .map((letter, index) => {
        if (letter === " " || letter === "\n") {
          return letter;
        }

        if (index / finalText.length < progress) {
          return letter;
        }

        return glyphs[Math.floor(Math.random() * glyphs.length)];
      })
      .join("");

    frame++;

    if (frame > frames) {
      window.clearInterval(scrambleTimer);

      scrambleTimer = null;

      element.classList.remove("is-scrambling");

      if (onDone) {
        onDone();
      }
    }
  }, frameTime);
}

function scrambleEnterOrbitText() {
  if (!enterOrbitButton || orbitEnterState.requested) {
    return;
  }

  /*
   * Duration reduced from 950ms → 560ms for a snappier
   * hover feel without sacrificing the character reveal.
   */
  scrambleTextElement(enterOrbitButton, "ENTER ORBIT", 560, () => {
    enterOrbitButton.textContent = "ENTER ORBIT";
  });
}

function scrambleTaglineLines(duration) {
  if (!tagline) {
    return;
  }

  const lines = Array.from(tagline.querySelectorAll("div"));

  const originals = lines.map((line) => line.innerHTML);

  const plainText = lines.map((line) => line.textContent || "");

  const glyphs = "01<>/\\[]{}";

  const frameTime = 70;

  const frames = Math.max(1, Math.round(duration / frameTime));

  let frame = 0;

  tagline.classList.add("is-scrambling");

  const timer = window.setInterval(() => {
    const progress = frame / frames;

    lines.forEach((line, lineIndex) => {
      const text = plainText[lineIndex];

      line.textContent = text
        .split("")
        .map((letter, index) => {
          if (letter === " ") {
            return " ";
          }

          if (index / text.length < progress) {
            return letter;
          }

          return glyphs[Math.floor(Math.random() * glyphs.length)];
        })
        .join("");
    });

    frame++;

    if (frame > frames) {
      window.clearInterval(timer);

      lines.forEach((line, lineIndex) => {
        line.innerHTML = originals[lineIndex];
      });

      tagline.classList.remove("is-scrambling");
    }
  }, frameTime);
}

function scrambleTaglineIn() {
  if (!tagline || taglineScrambled) {
    return;
  }

  taglineScrambled = true;

  scrambleTaglineLines(2600);
}

/* =========================================================
   ENTER ORBIT
   ========================================================= */

const orbitEnterState = {
  requested: false,

  completed: false,

  startedAt: 0,

  cameraStartZ: 8,

  cameraStartFov: 32,

  cameraTargetZ: 1.15,

  cameraTargetFov: 52,
};

window.orbitEnterState = orbitEnterState;

function requestOrbitEntry() {
  if (!enterOrbitButton || orbitEnterState.requested) {
    return;
  }

  const now = clock.getElapsedTime();

  orbitEnterState.requested = true;

  orbitEnterState.completed = false;

  orbitEnterState.startedAt = now;

  orbitEnterState.cameraStartZ = camera.position.z;

  orbitEnterState.cameraStartFov = camera.fov;

  document.body.classList.add("orbit-transitioning");

  window.dispatchEvent(new CustomEvent("orbit:cta-enter"));
}

if (enterOrbitButton) {
  enterOrbitButton.addEventListener("mouseenter", scrambleEnterOrbitText);

  enterOrbitButton.addEventListener("focus", scrambleEnterOrbitText);

  enterOrbitButton.addEventListener("click", requestOrbitEntry);

  enterOrbitButton.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();

      requestOrbitEntry();
    }
  });
}

/* =========================================================
   ENTER ORBIT TRANSITION
   ========================================================= */

function clamp01(value) {
  return Math.min(Math.max(value, 0), 1);
}

function easeOutQuart(value) {
  return 1 - Math.pow(1 - value, 4);
}

let transitionProgress = 0;

function updateEnterOrbitTransition(elapsed) {
  if (!orbitEnterState.requested) {
    return;
  }

  targetRotation.x += (0 - targetRotation.x) * 0.08;

  targetRotation.y += (0 - targetRotation.y) * 0.08;

  const rawProgress = clamp01(
    (elapsed - orbitEnterState.startedAt - 0.35) / 1.25,
  );

  transitionProgress = easeOutQuart(rawProgress);

  if (rawProgress <= 0) {
    return;
  }

  const eased = transitionProgress;

  camera.position.z =
    orbitEnterState.cameraStartZ +
    (orbitEnterState.cameraTargetZ - orbitEnterState.cameraStartZ) * eased;

  camera.fov =
    orbitEnterState.cameraStartFov +
    (orbitEnterState.cameraTargetFov - orbitEnterState.cameraStartFov) * eased;

  camera.updateProjectionMatrix();

  orbitGroup.position.y = 0.47 - eased * 0.18;

  orbitGroup.scale.setScalar(getOrbitScale() * (1 + eased * 2.35));

  particles.material.opacity = 0.88 + eased * 0.7;

  microParticles.material.opacity = 0.7 + eased * 0.45;

  surfaceParticles.material.opacity = 0.68 + eased * 0.32;

  rimLight.intensity = 21 + eased * 28;

  cyanLight.intensity = 7 + eased * 18;

  fillLight.intensity = 4.6 - eased * 2.2;

  if (rawProgress >= 1 && !orbitEnterState.completed) {
    orbitEnterState.completed = true;

    document.body.classList.add("orbit-entered");

    window.dispatchEvent(new CustomEvent("orbit:entered"));
  }
}

/* =========================================================
   RESIZE
   ========================================================= */

function resize() {
  const width = window.innerWidth;

  const height = window.innerHeight;

  camera.aspect = width / height;

  camera.updateProjectionMatrix();

  renderer.setPixelRatio(
    Math.min(window.devicePixelRatio || 1, quality === "low" ? 1.35 : 2),
  );

  renderer.setSize(width, height, false);

  if (!orbitEnterState.requested) {
    orbitGroup.scale.setScalar(getOrbitScale());
  }
}

window.addEventListener("resize", resize, {
  passive: true,
});

/* =========================================================
   CLOCK
   ========================================================= */

const clock = new THREE.Clock();

let sceneReady = false;

let introStartedAt = null;

let uiRevealed = false;

/* =========================================================
   ANIMATION LOOP
   ========================================================= */

function animate() {
  requestAnimationFrame(animate);

  const elapsed = clock.getElapsedTime();

  /* =======================================================
     MOUSE TILT
     ======================================================= */

  currentRotation.x += (targetRotation.x - currentRotation.x) * 0.035;

  currentRotation.y += (targetRotation.y - currentRotation.y) * 0.035;

  orbitGroup.rotation.x = currentRotation.x;

  orbitGroup.rotation.y = currentRotation.y;

  /* =======================================================
     ⭐ MAIN LIVING O ⭐
     ======================================================= */

  updateFacetInteraction(elapsed, uiRevealed);

  updateLivingO(elapsed);

  /* =======================================================
     O SUBTLE ROTATION
     ======================================================= */

  orbitO.rotation.z = Math.sin(elapsed * 0.22) * 0.018;

  /* =======================================================
     AMBIENT PARTICLES
     ======================================================= */

  particles.rotation.y = elapsed * 0.012;

  particles.rotation.x = Math.sin(elapsed * 0.08) * 0.018;

  crossParticles.rotation.z = Math.sin(elapsed * 0.12) * 0.012;

  crossParticles.rotation.y = elapsed * 0.008;

  microParticles.rotation.y = -elapsed * 0.006;

  microParticles.rotation.x = Math.sin(elapsed * 0.06) * 0.008;

  surfaceParticles.rotation.y = elapsed * 0.025;

  surfaceParticles.rotation.x = Math.sin(elapsed * 0.13) * 0.018;

  /* =======================================================
     ORBITAL CURVES
     ======================================================= */

  updateOrbitalElements(elapsed);

  /* =======================================================
     FLOATING FRAGMENTS
     ======================================================= */

  updateFragments(elapsed);

  /* =======================================================
     MOVING LIGHTS
     ======================================================= */

  rimLight.position.x = 3.8 + Math.sin(elapsed * 0.25) * 0.55;

  rimLight.position.y = 0.6 + Math.cos(elapsed * 0.2) * 0.45;

  cyanLight.position.x = -3.5 + Math.cos(elapsed * 0.18) * 0.5;

  /* =======================================================
     REFLECTION
     ======================================================= */

  reflectionPlane.material.opacity =
    (quality === "high" ? 0.7 : 0.45) + Math.sin(elapsed * 0.45) * 0.025;

  /* =======================================================
     ENTER TRANSITION
     ======================================================= */

  updateEnterOrbitTransition(elapsed);

  /* =======================================================
     RENDER
     ======================================================= */

  renderer.render(scene, camera);

  /* =======================================================
     LOADER
     ======================================================= */

  if (!sceneReady) {
    sceneReady = true;

    if (typeof window.completeOrbitLoader === "function") {
      window.completeOrbitLoader();
    }
  }
}

animate();

/* =========================================================
   PUBLIC API
   ========================================================= */

export {
  scene,
  camera,
  renderer,
  orbitGroup,
  orbitO,
  particles,
  crossParticles,
  microParticles,
  surfaceParticles,
};

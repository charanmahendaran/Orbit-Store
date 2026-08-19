import { resolvePreset, MODE_DRAWS } from "thinking-orbs/engine";

/* ========================================
   ORBIT THINKING ORB LOADER
   ======================================== */

/* ----------------------------------------
   DOM
---------------------------------------- */

const canvas = document.getElementById("thinking-orb");

const percentageElement = document.getElementById("orb-loading-percentage");

const loader = document.getElementById("orb-loader");

const startup = document.querySelector(".startup");

const ctx = canvas.getContext("2d", {
  alpha: true,
});

/* ========================================
   CONFIGURATION
   ======================================== */

const PRESET_SIZE = 64;

/*
 * Single loading colour.
 */

const ACCENT = {
  r: 32,
  g: 201,
  b: 195,
};

/*
 * Visual sweep timing.
 *
 * One progress step corresponds to
 * one complete sweep.
 */

const SWEEP_DURATION = 1.4;

const COMPLETE_HOLD = 0.18;

const CYCLE_DURATION = SWEEP_DURATION + COMPLETE_HOLD;

/*
 * Minimum number of visual sweeps.
 */

const MINIMUM_SWEEPS = 2;

/*
 * Maximum supported progress divisions.
 */

const MAX_SWEEPS = 8;

/*
 * Keep the loader at this value if
 * real loading takes unexpectedly long.
 */

const SOFT_CAP = 95;

/*
 * Time percentage remains at 100%
 * before the transition begins.
 */

const FINAL_HOLD = 0.45;

/*
 * Loader fade.

 * We want the orb to disappear first.
 */

const LOADER_FADE_DURATION = 900;

/*
 * IMPORTANT:
 *
 * Delay AFTER the loader disappears
 * and BEFORE startup begins.
 *
 * This creates the deliberate dark pause
 * you asked for.
 */

const STARTUP_DELAY = 700;

/*
 * Startup page reveal.

 * Slower than before.
 */

const STARTUP_REVEAL_DURATION = 1400;

/* ========================================
   TEST CONFIGURATION
   ======================================== */

/*
 * THIS is what you change for testing.
 *
 * 2 = 50 / 100
 * 3 = 33 / 67 / 100
 * 4 = 25 / 50 / 75 / 100
 * 5 = 20 / 40 / 60 / 80 / 100
 * 6 = 17 / 33 / 50 / 67 / 83 / 100
 *
 * The value is now the EXACT number
 * of progress sweeps.
 */

const TEST_SWEEPS = 6;

/*
 * Exact simulated loading duration.
 */

const TEST_LOAD_TIME = TEST_SWEEPS * CYCLE_DURATION;

/* ========================================
   DEVICE PIXEL RATIO
   ======================================== */

const dpr = Math.min(2, window.devicePixelRatio || 1);

/* ========================================
   SEARCHING PRESET
   ======================================== */

const { mode, speed, opts } = resolvePreset("searching", PRESET_SIZE);

const draw = MODE_DRAWS[mode];

/* ========================================
   RESPONSIVE ORB SIZE
   ======================================== */

function getOrbSize() {
  const width = window.innerWidth;

  const height = window.innerHeight;

  const smallest = Math.min(width, height);

  return Math.round(Math.max(112, Math.min(180, smallest * 0.16)));
}

let orbSize = getOrbSize();

/* ========================================
   CANVAS RESIZE
   ======================================== */

function resizeCanvas() {
  orbSize = getOrbSize();

  canvas.width = Math.round(orbSize * dpr);

  canvas.height = Math.round(orbSize * dpr);

  canvas.style.width = `${orbSize}px`;

  canvas.style.height = `${orbSize}px`;
}

resizeCanvas();

window.addEventListener("resize", resizeCanvas, {
  passive: true,
});

/* ========================================
   LOADING STATE
   ======================================== */

/*
 * Number of completed visual sweeps.
 */

let completedSweeps = 0;

/*
 * Last sweep cycle we registered.
 */

let lastCycle = -1;

/*
 * Actual application readiness.
 */

let applicationReady = false;

/*
 * Prevent duplicate ready signals.
 */

let readySignalReceived = false;

/*
 * Transition already started?
 */

let finishing = false;

/*
 * Current displayed percentage.
 */

let displayedProgress = 0;

/*
 * Time at which animation began.
 */

const startTime = performance.now();

/* ========================================
   PERCENTAGE
   ======================================== */

function setPercentage(value) {
  const rounded = Math.round(Math.max(0, Math.min(100, value)));

  /*
   * Never move backwards.
   */

  if (rounded < displayedProgress) {
    return;
  }

  displayedProgress = rounded;

  percentageElement.textContent = `${rounded}%`;
}

/* ========================================
   APPLICATION READY
   ======================================== */

/*
 * Later, the actual application will call:
 *
 * window.completeOrbitLoader();
 *
 * For the prototype, the timer below
 * calls it automatically.
 */

window.completeOrbitLoader = function completeOrbitLoader() {
  if (readySignalReceived) {
    return;
  }

  readySignalReceived = true;

  applicationReady = true;
};

/* ========================================
   PROTOTYPE READINESS
   ======================================== */

/*
 * We intentionally DO NOT use this timer
 * to decide the displayed percentage.
 *
 * It ONLY tells the loader:
 *
 * "The application is ready now."
 *
 * The sweep counter remains responsible
 * for percentage changes.
 */

setTimeout(() => {
  window.completeOrbitLoader();
}, TEST_LOAD_TIME);

/* ========================================
   SWEEP COUNTER
   ======================================== */

/*
 * IMPORTANT:
 *
 * Progress steps are based on elapsed
 * sweep cycles from OUR stable clock.
 *
 * They are NOT based on when the
 * application-ready event fires.
 */

function updateSweepCount(elapsedTime) {
  const currentCycle = Math.floor(elapsedTime / CYCLE_DURATION);

  /*
   * No new cycle.
   */

  if (currentCycle === lastCycle) {
    return false;
  }

  /*
   * First cycle starts at zero.
   */

  if (lastCycle === -1) {
    lastCycle = currentCycle;

    return false;
  }

  /*
   * Register every newly completed
   * cycle.
   */

  const newCycles = currentCycle - lastCycle;

  completedSweeps += newCycles;

  lastCycle = currentCycle;

  /*
   * Never exceed the configured
   * prototype sweep count.
   */

  completedSweeps = Math.min(completedSweeps, TEST_SWEEPS);

  return true;
}

/* ========================================
   PROGRESS
   ======================================== */

/*
 * This is now deliberately simple.
 *
 * TEST_SWEEPS = 4:
 *
 * completed = 0 → 0%
 * completed = 1 → 25%
 * completed = 2 → 50%
 * completed = 3 → 75%
 * completed = 4 → 100%
 */

function calculateProgress() {
  if (completedSweeps <= 0) {
    return 0;
  }

  /*
   * During loading:
   *
   * never exceed 95%.
   */

  if (!applicationReady) {
    const progress = (completedSweeps / TEST_SWEEPS) * 100;

    return Math.min(SOFT_CAP, progress);
  }

  /*
   * Application is ready.
   *
   * But percentage STILL depends on
   * completed sweeps.
   *
   * We do NOT instantly jump to 100%.
   */

  const progress = (completedSweeps / TEST_SWEEPS) * 100;

  return Math.min(100, progress);
}

/* ========================================
   STRAIGHT FILL
   ======================================== */

function getFillBoundary(progress) {
  return -1 + (progress / 100) * 2;
}

/* ========================================
   PROCESS STATIC COLOR
   ======================================== */

function processFrame() {
  const image = ctx.getImageData(0, 0, canvas.width, canvas.height);

  const pixels = image.data;

  const progress = calculateProgress();

  const fillBoundary = getFillBoundary(progress);

  /*
   * Process each particle.
   */

  for (let y = 0; y < canvas.height; y++) {
    for (let x = 0; x < canvas.width; x++) {
      const index = (y * canvas.width + x) * 4;

      const alpha = pixels[index + 3];

      if (alpha < 5) {
        continue;
      }

      const px = x / dpr;

      const py = y / dpr;

      const center = orbSize / 2;

      const dx = px - center;

      const dy = py - center;

      /*
       * Globe boundary.
       */

      const radius = Math.sqrt(dx * dx + dy * dy);

      if (radius > orbSize * 0.49) {
        continue;
      }

      /*
       * ----------------------------------
       * STRAIGHT VERTICAL BOUNDARY
       * ----------------------------------
       */

      const normalizedX = dx / (orbSize * 0.48);

      const isFilled = normalizedX <= fillBoundary;

      /*
       * Original particle brightness.
       */

      const brightness =
        (pixels[index] + pixels[index + 1] + pixels[index + 2]) / 3;

      let red = brightness;

      let green = brightness;

      let blue = brightness;

      /*
       * ----------------------------------
       * STATIC TEAL
       * ----------------------------------
       */

      if (isFilled) {
        const strength = 0.84;

        red = brightness * (1 - strength) + ACCENT.r * strength;

        green = brightness * (1 - strength) + ACCENT.g * strength;

        blue = brightness * (1 - strength) + ACCENT.b * strength;
      }

      /*
       * NO SECOND SWEEP.
       *
       * The Thinking Orbs Searching
       * animation underneath this layer
       * provides the ONLY moving sweep.
       */

      pixels[index] = Math.min(255, red);

      pixels[index + 1] = Math.min(255, green);

      pixels[index + 2] = Math.min(255, blue);
    }
  }

  ctx.putImageData(image, 0, 0);

  /*
   * Update percentage.
   */

  setPercentage(progress);
}

/* ========================================
   FINISH
   ======================================== */

function finishLoader() {
  if (finishing) {
    return;
  }

  /*
   * Application must be ready.
   */

  if (!applicationReady) {
    return;
  }

  /*
   * We MUST have completed every
   * configured sweep.
   */

  if (completedSweeps < TEST_SWEEPS) {
    return;
  }

  finishing = true;

  /*
   * Force exact 100%.
   */

  setPercentage(100);

  /*
   * --------------------------------------
   * HOLD 100%
   * --------------------------------------
   */

  setTimeout(() => {
    /*
     * ----------------------------------
     * FADE ORB OUT
     * ----------------------------------
     */

    loader.style.transition = `opacity ${LOADER_FADE_DURATION}ms cubic-bezier(0.22, 1, 0.36, 1)`;

    loader.style.opacity = "0";

    /*
     * ----------------------------------
     * WAIT AFTER LOADER
     * ----------------------------------
     *
     * This is intentional.
     *
     * We want a dark pause before the
     * ORBIT startup logo arrives.
     */

    setTimeout(() => {
      startup.style.visibility = "visible";

      startup.style.transition = `opacity ${STARTUP_REVEAL_DURATION}ms cubic-bezier(0.22, 1, 0.36, 1)`;

      startup.style.opacity = "1";
    }, LOADER_FADE_DURATION + STARTUP_DELAY);

    /*
     * Remove loader after its own fade.
     */

    setTimeout(() => {
      loader.style.pointerEvents = "none";

      loader.style.display = "none";
    }, LOADER_FADE_DURATION);
  }, FINAL_HOLD * 1000);
}

/* ========================================
   ANIMATION
   ======================================== */

let animationFrame = null;

function animate() {
  /*
   * Elapsed time from loader startup.
   *
   * IMPORTANT:
   *
   * This is our stable sweep clock.
   */

  const elapsed = (performance.now() - startTime) / 1000;

  /*
   * Update completed sweep count.
   */

  updateSweepCount(elapsed);

  /*
   * Clear.
   */

  ctx.setTransform(1, 0, 0, 1, 0, 0);

  ctx.clearRect(0, 0, canvas.width, canvas.height);

  /*
   * ======================================
   * ORIGINAL THINKING ORB
   * ======================================
   *
   * This is the ONLY sweep.
   */

  ctx.save();

  ctx.scale(dpr, dpr);

  draw(ctx, orbSize, elapsed * speed, true, opts);

  ctx.restore();

  /*
   * ======================================
   * STATIC PROGRESS COLOR
   * ======================================
   */

  processFrame();

  /*
   * ======================================
   * FINISH CHECK
   * ======================================
   */

  finishLoader();

  /*
   * Continue.
   */

  animationFrame = requestAnimationFrame(animate);
}

/* ========================================
   START
   ======================================== */

animate();

/* ========================================
   STOP
   ======================================== */

export function stopOrb() {
  if (animationFrame !== null) {
    cancelAnimationFrame(animationFrame);

    animationFrame = null;
  }
}

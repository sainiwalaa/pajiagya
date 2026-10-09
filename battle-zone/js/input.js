// Unified Mobile Touch & Desktop Keyboard/Mouse Input Manager

class InputManager {
  constructor() {
    this.moveForward = 0; // -1 to 1
    this.moveRight = 0;   // -1 to 1
    this.lookDeltaX = 0;
    this.lookDeltaY = 0;

    this.fire = false;
    this.aim = false;
    this.jump = false;
    this.crouch = false;
    this.sprint = false;
    this.reload = false;
    this.interact = false;
    this.switchWeaponSlot = null;

    this.isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
    this.isPointerLocked = false;

    // Mobile Touch tracking
    this.touchJoystickId = null;
    this.joystickOrigin = { x: 0, y: 0 };
    this.touchLookId = null;
    this.lookLastPos = { x: 0, y: 0 };

    this.setupDesktopControls();
    this.setupMobileControls();
  }

  setupDesktopControls() {
    window.addEventListener('keydown', (e) => {
      if (e.repeat) return;
      switch (e.code) {
        case 'KeyW': case 'ArrowUp': this.moveForward = 1; break;
        case 'KeyS': case 'ArrowDown': this.moveForward = -1; break;
        case 'KeyA': case 'ArrowLeft': this.moveRight = -1; break;
        case 'KeyD': case 'ArrowRight': this.moveRight = 1; break;
        case 'Space': this.jump = true; break;
        case 'KeyC': this.crouch = true; break;
        case 'ShiftLeft': case 'ShiftRight': this.sprint = true; break;
        case 'KeyR': this.reload = true; break;
        case 'KeyE': case 'KeyF': this.interact = true; break;
        case 'Digit1': this.switchWeaponSlot = 'primary'; break;
        case 'Digit2': this.switchWeaponSlot = 'secondary'; break;
        case 'Tab':
          e.preventDefault();
          if (window.uiManager) window.uiManager.toggleInventory();
          break;
        case 'Escape':
          if (window.uiManager) window.uiManager.togglePauseMenu();
          break;
      }
    });

    window.addEventListener('keyup', (e) => {
      switch (e.code) {
        case 'KeyW': case 'ArrowUp':
          if (this.moveForward > 0) this.moveForward = 0;
          break;
        case 'KeyS': case 'ArrowDown':
          if (this.moveForward < 0) this.moveForward = 0;
          break;
        case 'KeyA': case 'ArrowLeft':
          if (this.moveRight < 0) this.moveRight = 0;
          break;
        case 'KeyD': case 'ArrowRight':
          if (this.moveRight > 0) this.moveRight = 0;
          break;
        case 'ShiftLeft': case 'ShiftRight':
          this.sprint = false;
          break;
      }
    });

    // Mouse Controls
    window.addEventListener('mousedown', (e) => {
      if (e.target.closest('#hud') || e.target.closest('.modal')) return;

      if (e.button === 0) { // Left Click = Fire
        this.fire = true;
      } else if (e.button === 2) { // Right Click = Aim
        e.preventDefault();
        this.aim = true;
      }
    });

    window.addEventListener('mouseup', (e) => {
      if (e.button === 0) this.fire = false;
      if (e.button === 2) this.aim = false;
    });

    window.addEventListener('contextmenu', (e) => e.preventDefault());

    // Pointer Lock for FPS mouse look
    document.addEventListener('mousemove', (e) => {
      if (document.pointerLockElement) {
        this.lookDeltaX += e.movementX;
        this.lookDeltaY += e.movementY;
      }
    });
  }

  setupMobileControls() {
    const joyZone = document.getElementById('joystick-zone');
    const joyKnob = document.getElementById('joystick-knob');
    const lookZone = document.getElementById('look-zone');

    if (!joyZone || !lookZone) return;

    // Joystick Touch
    joyZone.addEventListener('touchstart', (e) => {
      e.preventDefault();
      const touch = e.changedTouches[0];
      this.touchJoystickId = touch.identifier;
      const rect = joyZone.getBoundingClientRect();
      this.joystickOrigin = {
        x: rect.left + rect.width / 2,
        y: rect.top + rect.height / 2
      };
      this.handleJoystickMove(touch.clientX, touch.clientY, joyKnob);
    }, { passive: false });

    joyZone.addEventListener('touchmove', (e) => {
      e.preventDefault();
      for (let i = 0; i < e.changedTouches.length; i++) {
        const touch = e.changedTouches[i];
        if (touch.identifier === this.touchJoystickId) {
          this.handleJoystickMove(touch.clientX, touch.clientY, joyKnob);
          break;
        }
      }
    }, { passive: false });

    const endJoystick = (e) => {
      for (let i = 0; i < e.changedTouches.length; i++) {
        if (e.changedTouches[i].identifier === this.touchJoystickId) {
          this.touchJoystickId = null;
          this.moveForward = 0;
          this.moveRight = 0;
          if (joyKnob) joyKnob.style.transform = `translate(0px, 0px)`;
          break;
        }
      }
    };
    joyZone.addEventListener('touchend', endJoystick);
    joyZone.addEventListener('touchcancel', endJoystick);

    // Look / Drag camera touch zone
    lookZone.addEventListener('touchstart', (e) => {
      for (let i = 0; i < e.changedTouches.length; i++) {
        const touch = e.changedTouches[i];
        if (this.touchLookId === null) {
          this.touchLookId = touch.identifier;
          this.lookLastPos = { x: touch.clientX, y: touch.clientY };
          break;
        }
      }
    }, { passive: true });

    lookZone.addEventListener('touchmove', (e) => {
      for (let i = 0; i < e.changedTouches.length; i++) {
        const touch = e.changedTouches[i];
        if (touch.identifier === this.touchLookId) {
          const dx = touch.clientX - this.lookLastPos.x;
          const dy = touch.clientY - this.lookLastPos.y;
          this.lookDeltaX += dx * 1.5;
          this.lookDeltaY += dy * 1.5;
          this.lookLastPos = { x: touch.clientX, y: touch.clientY };
          break;
        }
      }
    }, { passive: true });

    const endLook = (e) => {
      for (let i = 0; i < e.changedTouches.length; i++) {
        if (e.changedTouches[i].identifier === this.touchLookId) {
          this.touchLookId = null;
          break;
        }
      }
    };
    lookZone.addEventListener('touchend', endLook);
    lookZone.addEventListener('touchcancel', endLook);

    // Mobile Action Buttons bindings
    this.bindButton('btn-fire', (down) => { this.fire = down; });
    this.bindButton('btn-aim', (down) => { if (down) this.aim = !this.aim; });
    this.bindButton('btn-jump', (down) => { if (down) this.jump = true; });
    this.bindButton('btn-crouch', (down) => { if (down) this.crouch = true; });
    this.bindButton('btn-sprint', (down) => { this.sprint = down; });
    this.bindButton('btn-reload', (down) => { if (down) this.reload = true; });
    this.bindButton('btn-interact', (down) => { if (down) this.interact = true; });
  }

  bindButton(elementId, callback) {
    const el = document.getElementById(elementId);
    if (!el) return;

    el.addEventListener('touchstart', (e) => {
      e.preventDefault();
      e.stopPropagation();
      callback(true);
    }, { passive: false });

    el.addEventListener('touchend', (e) => {
      e.preventDefault();
      e.stopPropagation();
      callback(false);
    }, { passive: false });

    el.addEventListener('mousedown', (e) => {
      e.preventDefault();
      callback(true);
    });

    el.addEventListener('mouseup', (e) => {
      e.preventDefault();
      callback(false);
    });
  }

  handleJoystickMove(touchX, touchY, knob) {
    const maxRadius = 50;
    const dx = touchX - this.joystickOrigin.x;
    const dy = touchY - this.joystickOrigin.y;
    const dist = Math.sqrt(dx * dx + dy * dy);

    const clampedDist = Math.min(dist, maxRadius);
    const angle = Math.atan2(dy, dx);

    const knobX = Math.cos(angle) * clampedDist;
    const knobY = Math.sin(angle) * clampedDist;

    if (knob) knob.style.transform = `translate(${knobX}px, ${knobY}px)`;

    // Map to moveForward & moveRight
    this.moveRight = knobX / maxRadius;
    this.moveForward = -knobY / maxRadius;
  }
}

window.InputManager = InputManager;

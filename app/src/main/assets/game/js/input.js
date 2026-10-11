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

    this.isTouchDevice = ('ontouchstart' in window) || (typeof navigator !== 'undefined' && navigator.maxTouchPoints > 0);
    this.isPointerLocked = false;

    // Mobile Touch tracking
    this.touchJoystickId = null;
    this.joystickOrigin = { x: 0, y: 0 };
    this.touchLookId = null;
    this.lookLastPos = { x: 0, y: 0 };

    this.activeKeys = new Set();
    this.setupDesktopControls();
    this.setupMobileControls();
  }

  setupDesktopControls() {
    window.addEventListener('keydown', (e) => {
      if (e.code) this.activeKeys.add(e.code);
      if (e.key) this.activeKeys.add(e.key.toLowerCase());
      this.syncMovementFromKeys();

      const code = e.code || '';
      const key = (e.key || '').toLowerCase();

      if (code === 'Space' || key === ' ') {
        this.jump = true;
      } else if (code === 'KeyC' || key === 'c') {
        this.crouch = true;
      } else if (code === 'ShiftLeft' || code === 'ShiftRight' || key === 'shift') {
        this.sprint = true;
      } else if (code === 'KeyR' || key === 'r') {
        this.reload = true;
      } else if (code === 'KeyE' || key === 'e' || code === 'KeyF' || key === 'f') {
        this.interact = true;
      } else if (code === 'Digit1' || key === '1') {
        this.switchWeaponSlot = 'primary';
      } else if (code === 'Digit2' || key === '2') {
        this.switchWeaponSlot = 'secondary';
      } else if (code === 'KeyQ' || key === 'q') {
        if (window.gameInstance && window.gameInstance.player) {
          window.gameInstance.player.swapWeapons();
        }
      } else if (code === 'Tab' || key === 'tab') {
        e.preventDefault();
        if (window.uiManager) window.uiManager.toggleInventory();
      } else if (code === 'Escape' || key === 'escape') {
        if (document.pointerLockElement) {
          document.exitPointerLock();
        } else if (window.uiManager) {
          window.uiManager.togglePauseMenu();
        }
      } else if (code === 'F3' || key === 'f3' || code === 'Backquote' || key === '`') {
        e.preventDefault();
        if (window.uiManager) window.uiManager.toggleDebugOverlay();
      }
    });

    window.addEventListener('keyup', (e) => {
      if (e.code) this.activeKeys.delete(e.code);
      if (e.key) this.activeKeys.delete(e.key.toLowerCase());
      this.syncMovementFromKeys();

      const code = e.code || '';
      const key = (e.key || '').toLowerCase();
      if (code === 'ShiftLeft' || code === 'ShiftRight' || key === 'shift') {
        this.sprint = false;
      }
    });

    window.addEventListener('blur', () => {
      this.activeKeys.clear();
      this.moveForward = 0;
      this.moveRight = 0;
      this.fire = false;
      this.aim = false;
      this.sprint = false;
    });

    // Pointer Lock change listener
    document.addEventListener('pointerlockchange', () => {
      this.isPointerLocked = !!document.pointerLockElement;
    });

    // Mouse Controls
    let isMouseDown = false;
    let mouseLastX = 0;
    let mouseLastY = 0;

    window.addEventListener('mousedown', (e) => {
      if (e.target.closest('#hud button') || e.target.closest('.modal') || e.target.closest('#screen-menu') || e.target.closest('.action-btn')) {
        return;
      }

      isMouseDown = true;
      mouseLastX = e.clientX;
      mouseLastY = e.clientY;

      // Request pointer lock on desktop when clicking viewport during game
      const container = document.getElementById('canvas-container');
      const gameScreen = document.getElementById('screen-game');
      if (container && gameScreen && gameScreen.classList.contains('active') && !document.pointerLockElement && e.button === 0) {
        try {
          container.requestPointerLock();
        } catch (_) {}
      }

      if (e.button === 0) {
        this.fire = true;
      } else if (e.button === 2) {
        e.preventDefault();
        this.aim = true;
      }
    });

    window.addEventListener('mouseup', (e) => {
      isMouseDown = false;
      if (e.button === 0) this.fire = false;
      if (e.button === 2) this.aim = false;
    });

    window.addEventListener('contextmenu', (e) => {
      const gameScreen = document.getElementById('screen-game');
      if (gameScreen && gameScreen.classList.contains('active')) {
        e.preventDefault();
      }
    });

    // Mouse move handling (pointer lock and drag-to-look fallback)
    window.addEventListener('mousemove', (e) => {
      if (document.pointerLockElement) {
        this.lookDeltaX += e.movementX;
        this.lookDeltaY += e.movementY;
      } else if (isMouseDown) {
        const dx = e.clientX - mouseLastX;
        const dy = e.clientY - mouseLastY;
        mouseLastX = e.clientX;
        mouseLastY = e.clientY;
        this.lookDeltaX += dx * 1.3;
        this.lookDeltaY += dy * 1.3;
      }
    });
  }

  syncMovementFromKeys() {
    let forward = 0;
    let right = 0;

    const has = (...keys) => keys.some(k => this.activeKeys.has(k) || this.activeKeys.has(k.toLowerCase()));

    if (has('KeyW', 'ArrowUp', 'w')) forward += 1;
    if (has('KeyS', 'ArrowDown', 's')) forward -= 1;
    if (has('KeyD', 'ArrowRight', 'd')) right += 1;
    if (has('KeyA', 'ArrowLeft', 'a')) right -= 1;

    this.moveForward = forward;
    this.moveRight = right;
  }

  setupMobileControls() {
    const joyZone = document.getElementById('joystick-zone');
    const joyKnob = document.getElementById('joystick-knob');
    const lookZone = document.getElementById('look-zone');

    if (!joyZone || !lookZone) return;

    // Joystick Touch with robust tracking
    joyZone.addEventListener('touchstart', (e) => {
      e.preventDefault();
      e.stopPropagation();
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
      e.stopPropagation();
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
          if (joyKnob) joyKnob.style.transform = 'translate(0px, 0px)';
          break;
        }
      }
    };
    joyZone.addEventListener('touchend', endJoystick, { passive: false });
    joyZone.addEventListener('touchcancel', endJoystick, { passive: false });

    // Look / Drag camera touch zone
    lookZone.addEventListener('touchstart', (e) => {
      e.preventDefault();
      for (let i = 0; i < e.changedTouches.length; i++) {
        const touch = e.changedTouches[i];
        if (this.touchLookId === null) {
          this.touchLookId = touch.identifier;
          this.lookLastPos = { x: touch.clientX, y: touch.clientY };
          break;
        }
      }
    }, { passive: false });

    lookZone.addEventListener('touchmove', (e) => {
      e.preventDefault();
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
    }, { passive: false });

    const endLook = (e) => {
      for (let i = 0; i < e.changedTouches.length; i++) {
        if (e.changedTouches[i].identifier === this.touchLookId) {
          this.touchLookId = null;
          break;
        }
      }
    };
    lookZone.addEventListener('touchend', endLook, { passive: false });
    lookZone.addEventListener('touchcancel', endLook, { passive: false });

    // Mobile Action Buttons bindings
    this.bindButton('btn-fire', (down) => { this.fire = down; });
    this.bindButton('btn-aim', (down) => { if (down) this.aim = !this.aim; });
    this.bindButton('btn-jump', (down) => { if (down) this.jump = true; });
    this.bindButton('btn-crouch', (down) => { if (down) this.crouch = true; });
    this.bindButton('btn-sprint', (down) => {
      if (down) {
        this.sprint = !this.sprint;
        const btn = document.getElementById('btn-sprint');
        if (btn) btn.classList.toggle('active', this.sprint);
        if (window.audio && typeof window.audio.playPickup === 'function') {
          window.audio.playPickup();
        }
      }
    });
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

    el.addEventListener('touchcancel', (e) => {
      e.preventDefault();
      e.stopPropagation();
      callback(false);
    }, { passive: false });

    el.addEventListener('mousedown', (e) => {
      e.preventDefault();
      e.stopPropagation();
      callback(true);
    });

    el.addEventListener('mouseup', (e) => {
      e.preventDefault();
      e.stopPropagation();
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

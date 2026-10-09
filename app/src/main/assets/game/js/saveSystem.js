// LocalStorage Save & Progression Manager
// Persists game progress, unlocked levels, weapons, and settings

class SaveSystem {
  constructor() {
    this.STORAGE_KEY = 'battle_zone_survival_save_v1';
    this.data = this.load();
  }

  getDefaults() {
    return {
      unlockedLevel: 1,
      completedMissions: [],
      xp: 0,
      coins: 500,
      totalKills: 0,
      matchesPlayed: 0,
      victories: 0,
      unlockedWeapons: ['rifle', 'pistol'],
      equippedPrimary: 'rifle',
      equippedSecondary: 'pistol',
      settings: {
        sensitivity: 1.0,
        graphics: 'high', // 'low', 'medium', 'high'
        soundVolume: 0.8,
        musicVolume: 0.5,
        hapticsEnabled: true
      }
    };
  }

  load() {
    try {
      const raw = localStorage.getItem(this.STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        return Object.assign(this.getDefaults(), parsed);
      }
    } catch (e) {
      console.warn("Failed to load save data from localStorage", e);
    }
    return this.getDefaults();
  }

  save() {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(this.data));
    } catch (e) {
      console.warn("Failed to save data to localStorage", e);
    }
  }

  addReward(xpEarned, coinsEarned, kills = 0) {
    this.data.xp += xpEarned;
    this.data.coins += coinsEarned;
    this.data.totalKills += kills;
    this.save();
  }

  unlockLevel(levelNumber) {
    if (levelNumber > this.data.unlockedLevel) {
      this.data.unlockedLevel = Math.min(levelNumber, 5);
      this.save();
    }
  }

  completeMission(missionId) {
    if (!this.data.completedMissions.includes(missionId)) {
      this.data.completedMissions.push(missionId);
      this.save();
    }
  }

  unlockWeapon(weaponId) {
    if (!this.data.unlockedWeapons.includes(weaponId)) {
      this.data.unlockedWeapons.push(weaponId);
      this.save();
    }
  }

  setEquippedWeapon(slot, weaponId) {
    if (slot === 'primary') this.data.equippedPrimary = weaponId;
    if (slot === 'secondary') this.data.equippedSecondary = weaponId;
    this.save();
  }

  updateSettings(newSettings) {
    this.data.settings = Object.assign(this.data.settings, newSettings);
    this.save();
  }

  resetProgress() {
    this.data = this.getDefaults();
    this.save();
  }
}

window.saveSystem = new SaveSystem();

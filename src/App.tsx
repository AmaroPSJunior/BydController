/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Power, Battery, Wifi, RefreshCw, Lightbulb, ShieldCheck, Settings, X } from 'lucide-react';
import { bydService, type VehicleState } from './services/bydService';

export default function App() {
  const [vehicleState, setVehicleState] = useState<VehicleState>(bydService.getState());
  const [isToggling, setIsToggling] = useState(false);
  const [showSettings, setShowSettings] = useState(false);

  const handleToggle = async () => {
    if (isToggling) return;
    setIsToggling(true);
    try {
      await bydService.toggleInternalLights();
      setVehicleState(bydService.getState());
    } finally {
      setIsToggling(false);
    }
  };

  useEffect(() => {
    const interval = setInterval(() => {
      setVehicleState(bydService.getState());
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="h-screen w-full flex flex-col bg-bg-deep text-white font-sans overflow-hidden">
      {/* Header */}
      <header className="h-20 border-b border-grid-line flex justify-between items-center px-10 shrink-0">
        <div className="text-xl font-bold tracking-[0.25em] text-accent-blue">BYD DOLPHIN PLUS</div>
        <div className="flex gap-6 text-[11px] tracking-widest text-text-dim uppercase items-center">
          <span>{new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}</span>
          <span>24°C</span>
          <span className="text-accent-blue flex items-center gap-2">
            <span className="w-1.5 h-1.5 bg-accent-blue rounded-full" />
            API CONNECTED
          </span>
          <span>4G LTE</span>
          <button 
            onClick={() => setShowSettings(true)}
            className="p-1 hover:text-white transition-colors"
          >
            <Settings size={14} />
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1 grid grid-cols-[320px_1fr_320px] overflow-hidden">
        {/* Left Side Panel */}
        <div className="side-panel border-r">
          <div className="info-card">
            <div className="text-[10px] uppercase text-text-dim tracking-widest mb-2 font-bold">Bateria do Veículo</div>
            <div className="text-3xl font-light">{vehicleState.batteryLevel}%</div>
            <div className="h-1 bg-white/10 mt-4 overflow-hidden">
              <motion.div 
                className="h-full bg-accent-blue"
                animate={{ width: `${vehicleState.batteryLevel}%` }}
              />
            </div>
          </div>
          <div className="info-card !border-l-text-dim">
            <div className="text-[10px] uppercase text-text-dim tracking-widest mb-2 font-bold">Autonomia Estimada</div>
            <div className="text-3xl font-light">358 km</div>
          </div>
        </div>

        {/* Center Stage */}
        <div className="flex flex-col items-center justify-center relative p-10">
          <div className="relative w-80 h-80 rounded-full border border-grid-line flex items-center justify-center bg-[radial-gradient(circle,rgba(0,229,255,0.05)_0%,transparent_70%)]">
            <motion.button
              onClick={handleToggle}
              disabled={isToggling}
              className={`w-48 h-48 rounded-full bg-bg-card border-4 ${vehicleState.internalLights ? 'border-accent-blue shadow-[0_0_40px_rgba(0,229,255,0.2)]' : 'border-white/10 opacity-60'} flex flex-col items-center justify-center transition-all duration-500`}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              <AnimatePresence mode="wait">
                {isToggling ? (
                  <motion.div
                    key="loading"
                    animate={{ rotate: 360 }}
                    transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                  >
                    <RefreshCw size={40} className="text-accent-blue/40" />
                  </motion.div>
                ) : (
                  <motion.div
                    key="icon"
                    className="flex flex-col items-center gap-3"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                  >
                    <Power size={40} className={vehicleState.internalLights ? 'text-accent-blue' : 'text-white/20'} />
                    <div className="text-xs font-bold tracking-[0.15em]">
                      LUZES {vehicleState.internalLights ? 'ON' : 'OFF'}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.button>
            <div className={`absolute -bottom-5 bg-accent-blue text-bg-deep px-4 py-1 text-[9px] font-black uppercase tracking-widest rounded-full transition-opacity duration-300 ${vehicleState.internalLights ? 'opacity-100' : 'opacity-40'}`}>
              Lâmpadas Internas
            </div>
          </div>

          <div className="mt-16 opacity-30 flex flex-col items-center gap-4">
            <svg viewBox="0 0 100 40" className="w-[400px] text-text-dim">
              <path d="M10,30 Q10,10 30,10 L70,10 Q90,10 90,30 L10,30" stroke="currentColor" fill="none" strokeWidth="0.5" />
              <circle cx="25" cy="32" r="4" stroke="currentColor" fill="none" strokeWidth="0.5" />
              <circle cx="75" cy="32" r="4" stroke="currentColor" fill="none" strokeWidth="0.5" />
            </svg>
            <span className="text-[10px] uppercase tracking-[0.2em] font-medium">Controle Nativo de Iluminação</span>
          </div>
        </div>

        {/* Right Side Panel */}
        <div className="side-panel border-l">
          <div className="info-card">
            <div className="text-[10px] uppercase text-text-dim tracking-widest mb-2 font-bold">Consumo de API</div>
            <div className="text-sm font-mono text-white/90">BYD.CAN.BodyControl</div>
          </div>
          <div className="info-card">
            <div className="text-[10px] uppercase text-text-dim tracking-widest mb-2 font-bold">Modo de Condução</div>
            <div className="text-2xl font-light">Sport+</div>
          </div>
        </div>
        
        <div className="absolute bottom-5 right-5 text-[9px] font-mono text-text-dim opacity-50 tracking-tighter">
          v1.0.4-stable | local_host_comm
        </div>
      </main>

      {/* Footer Navigation */}
      <footer className="h-[100px] bg-bg-card border-t border-grid-line grid grid-cols-5 shrink-0">
        <div className="nav-item">
          <span className="text-xl mb-1">⌂</span>
          Home
        </div>
        <div className="nav-item nav-item-active">
          <span className="text-xl mb-1">☼</span>
          Luzes
        </div>
        <div className="nav-item">
          <span className="text-xl mb-1">♨</span>
          Clima
        </div>
        <div className="nav-item">
          <span className="text-xl mb-1">◓</span>
          Energia
        </div>
        <div className="nav-item border-r-0">
          <span className="text-xl mb-1">⚙</span>
          Ajustes
        </div>
      </footer>

      {/* Settings Modal */}
      <AnimatePresence>
        {showSettings && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-bg-deep/95 backdrop-blur-md"
          >
            <motion.div
              initial={{ scale: 0.9, y: 20 }}
              animate={{ scale: 1, y: 0 }}
              className="bg-bg-card p-10 rounded-sm border-l-4 border-accent-blue w-full max-w-md relative"
            >
              <button 
                onClick={() => setShowSettings(false)}
                className="absolute top-4 right-4 p-2 text-text-dim hover:text-white"
              >
                <X size={20} />
              </button>

              <h3 className="text-xl font-light mb-8 uppercase tracking-widest">Configuração <span className="font-bold text-accent-blue">API</span></h3>
              
              <div className="space-y-6">
                <div className="space-y-1.5">
                  <label className="text-[10px] uppercase font-bold text-text-dim tracking-widest">Usuário (BYD Cloud)</label>
                  <input 
                    type="text" 
                    placeholder="jose@email.com"
                    className="w-full bg-white/5 border border-white/5 rounded-none px-4 py-4 text-sm focus:outline-none focus:border-accent-blue/50 transition-colors"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-[10px] uppercase font-bold text-text-dim tracking-widest">Senha de Acesso</label>
                  <input 
                    type="password" 
                    placeholder="••••••••"
                    className="w-full bg-white/5 border border-white/5 rounded-none px-4 py-4 text-sm focus:outline-none focus:border-accent-blue/50 transition-colors"
                  />
                </div>
                <div className="pt-4">
                  <button className="w-full bg-accent-blue text-bg-deep py-4 font-black text-[10px] uppercase tracking-[0.2em] shadow-lg shadow-accent-blue/10 active:scale-95 transition-all">
                    Sincronizar Veículo
                  </button>
                </div>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

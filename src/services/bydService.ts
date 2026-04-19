/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * Interface para representar o estado do veículo.
 */
export interface VehicleState {
  internalLights: boolean;
  connected: boolean;
  batteryLevel: number;
  lastSync: Date;
}

/**
 * Serviço responsável pela integração com a API (simulada ou real) da BYD.
 * No futuro, este serviço pode ser estendido para utilizar bibliotecas como 'byd-api' ou
 * chamadas diretas para os endpoints de nuvem da BYD.
 */
class BYDService {
  private state: VehicleState = {
    internalLights: false,
    connected: true,
    batteryLevel: 85,
    lastSync: new Date(),
  };

  /**
   * Alterna o estado das luzes internas.
   * Nota: A maioria das APIs de nuvem da BYD não suporta controle de luzes de leitura,
   * mas pode suportar luzes ambientes ou sinalizadores.
   */
  async toggleInternalLights(): Promise<boolean> {
    // Simular latência de rede
    await new Promise((resolve) => setTimeout(resolve, 800));
    
    // Aqui seria feita a chamada para a API proprietária
    // Exemplo: await this.apiClient.controlLight(true);
    
    this.state.internalLights = !this.state.internalLights;
    this.state.lastSync = new Date();
    
    return this.state.internalLights;
  }

  getState(): VehicleState {
    return { ...this.state };
  }

  /**
   * Verifica a conexão com o veículo.
   */
  async checkConnection(): Promise<boolean> {
    // Simulação de check
    return true;
  }
}

export const bydService = new BYDService();

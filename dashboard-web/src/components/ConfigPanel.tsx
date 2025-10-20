import React, { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Label } from "./ui/label";
import { Slider } from "./ui/slider";
import { Switch } from "./ui/switch";
import { Button } from "./ui/button";
import { Settings, Save } from "lucide-react";

export function ConfigPanel() {
  const [riskThreshold, setRiskThreshold] = useState([75]);
  const [autoBlock, setAutoBlock] = useState(true);
  const [multiCountry, setMultiCountry] = useState(true);
  const [velocityCheck, setVelocityCheck] = useState(true);
  const [amountAnomaly, setAmountAnomaly] = useState(true);

  return (
    <Card className="col-span-3">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Settings className="h-5 w-5" />
          Configuración de Umbrales
        </CardTitle>
        <CardDescription>
          Ajusta los parámetros de detección del sistema
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <Label>Umbral de Riesgo</Label>
            <span className="text-sm text-muted-foreground">{riskThreshold[0]}%</span>
          </div>
          <Slider
            value={riskThreshold}
            onValueChange={setRiskThreshold}
            max={100}
            step={1}
            className="w-full"
          />
          <p className="text-xs text-muted-foreground">
            Transacciones con puntaje superior serán bloqueadas automáticamente
          </p>
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Bloqueo Automático</Label>
              <p className="text-xs text-muted-foreground">
                Bloquear transacciones de alto riesgo
              </p>
            </div>
            <Switch checked={autoBlock} onCheckedChange={setAutoBlock} />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Detección Multi-País</Label>
              <p className="text-xs text-muted-foreground">
                Alertar por transacciones simultáneas
              </p>
            </div>
            <Switch checked={multiCountry} onCheckedChange={setMultiCountry} />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Análisis de Velocidad</Label>
              <p className="text-xs text-muted-foreground">
                Detectar múltiples intentos rápidos
              </p>
            </div>
            <Switch checked={velocityCheck} onCheckedChange={setVelocityCheck} />
          </div>

          <div className="flex items-center justify-between">
            <div className="space-y-0.5">
              <Label>Anomalía de Montos</Label>
              <p className="text-xs text-muted-foreground">
                Detectar montos inusuales por usuario
              </p>
            </div>
            <Switch checked={amountAnomaly} onCheckedChange={setAmountAnomaly} />
          </div>
        </div>

        <Button className="w-full gap-2">
          <Save className="h-4 w-4" />
          Guardar Configuración
        </Button>
      </CardContent>
    </Card>
  );
}

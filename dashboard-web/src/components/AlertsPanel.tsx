import React from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { ScrollArea } from "./ui/scroll-area";
import { AlertTriangle, Clock, MapPin, DollarSign } from "lucide-react";

interface Alert {
  id: string;
  type: string;
  severity: "critical" | "high" | "medium";
  description: string;
  amount: string;
  location: string;
  timestamp: string;
}

const alerts: Alert[] = [
  {
    id: "ALT-001",
    type: "Transacción múltiple",
    severity: "critical",
    description: "5 transacciones desde diferentes países en 10 minutos",
    amount: "$12,450",
    location: "Nigeria, Rusia, China",
    timestamp: "Hace 2 min"
  },
  {
    id: "ALT-002",
    type: "Monto inusual",
    severity: "high",
    description: "Transacción 500% superior al promedio del usuario",
    amount: "$45,000",
    location: "Miami, USA",
    timestamp: "Hace 5 min"
  },
  {
    id: "ALT-003",
    type: "Patrón sospechoso",
    severity: "high",
    description: "Actividad similar a red de fraude conocida",
    amount: "$8,200",
    location: "São Paulo, Brasil",
    timestamp: "Hace 8 min"
  },
  {
    id: "ALT-004",
    type: "Cambio de comportamiento",
    severity: "medium",
    description: "Primera transacción internacional del usuario",
    amount: "$2,300",
    location: "Londres, UK",
    timestamp: "Hace 12 min"
  },
  {
    id: "ALT-005",
    type: "Velocidad alta",
    severity: "critical",
    description: "15 intentos de transacción en 60 segundos",
    amount: "$890 c/u",
    location: "México City, MX",
    timestamp: "Hace 15 min"
  },
];

export function AlertsPanel() {
  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "critical":
        return "destructive";
      case "high":
        return "default";
      case "medium":
        return "secondary";
      default:
        return "outline";
    }
  };

  return (
    <Card className="col-span-3">
      <CardHeader>
        <CardTitle>Alertas Activas</CardTitle>
        <CardDescription>
          Transacciones sospechosas detectadas por los agentes IA
        </CardDescription>
      </CardHeader>
      <CardContent>
        <ScrollArea className="h-[400px] pr-4">
          <div className="space-y-4">
            {alerts.map((alert) => (
              <div
                key={alert.id}
                className="flex items-start space-x-4 rounded-lg border p-4 hover:bg-accent/50 transition-colors"
              >
                <div className="mt-1">
                  <AlertTriangle className="h-5 w-5 text-destructive" />
                </div>
                <div className="flex-1 space-y-1">
                  <div className="flex items-center justify-between">
                    <p className="text-sm">
                      {alert.type}
                    </p>
                    <Badge variant={getSeverityColor(alert.severity)}>
                      {alert.severity.toUpperCase()}
                    </Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">
                    {alert.description}
                  </p>
                  <div className="flex items-center gap-4 pt-2 text-xs text-muted-foreground">
                    <span className="flex items-center gap-1">
                      <DollarSign className="h-3 w-3" />
                      {alert.amount}
                    </span>
                    <span className="flex items-center gap-1">
                      <MapPin className="h-3 w-3" />
                      {alert.location}
                    </span>
                    <span className="flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      {alert.timestamp}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </ScrollArea>
      </CardContent>
    </Card>
  );
}

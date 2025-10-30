import React from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from "recharts";

const data = [
  { time: "00:00", transacciones: 4200, fraudes: 12, anomalias: 45 },
  { time: "04:00", transacciones: 2100, fraudes: 8, anomalias: 23 },
  { time: "08:00", transacciones: 8900, fraudes: 28, anomalias: 87 },
  { time: "12:00", transacciones: 12400, fraudes: 35, anomalias: 112 },
  { time: "16:00", transacciones: 11200, fraudes: 42, anomalias: 98 },
  { time: "20:00", transacciones: 9800, fraudes: 31, anomalias: 76 },
  { time: "23:00", transacciones: 6500, fraudes: 19, anomalias: 54 },
];

export function FraudChart() {
  return (
    <Card className="col-span-4">
      <CardHeader>
        <CardTitle>Análisis de Detección en Tiempo Real</CardTitle>
        <CardDescription>
          Monitoreo de transacciones y detección de fraudes en las últimas 24 horas
        </CardDescription>
      </CardHeader>
      <CardContent className="pt-2">
        <ResponsiveContainer width="100%" height={350}>
          <AreaChart data={data}>
            <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
            <XAxis dataKey="time" className="text-xs" stroke="hsl(var(--muted-foreground))" />
            <YAxis className="text-xs" stroke="hsl(var(--muted-foreground))" />
            <Tooltip
              contentStyle={{
                backgroundColor: "hsl(var(--popover))",
                border: "1px solid hsl(var(--border))",
                borderRadius: "var(--radius)"
              }}
            />
            <Legend />
            <Area type="monotone" dataKey="transacciones" stackId="1" stroke="hsl(var(--chart-1))" fill="hsl(var(--chart-1))" fillOpacity={0.6} name="Transacciones" />
            <Area type="monotone" dataKey="anomalias" stackId="2" stroke="hsl(var(--chart-4))" fill="hsl(var(--chart-4))" fillOpacity={0.6} name="Anomalías" />
            <Area type="monotone" dataKey="fraudes" stackId="3" stroke="hsl(var(--destructive))" fill="hsl(var(--destructive))" fillOpacity={0.6} name="Fraudes Detectados" />
          </AreaChart>
        </ResponsiveContainer>
      </CardContent>
    </Card>
  );
}

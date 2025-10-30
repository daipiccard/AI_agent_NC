import React from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip, PieLabelRenderProps } from "recharts";

const data = [
  { name: "Bajo Riesgo (0-40)", value: 845, color: "#a78bfa" },
  { name: "Riesgo Medio (41-70)", value: 312, color: "#8b5cf6" },
  { name: "Alto Riesgo (71-89)", value: 156, color: "#7c3aed" },
  { name: "Riesgo Crítico (90+)", value: 87, color: "#dc2626" },
];

// Label personalizado
const renderCustomLabel = (props: PieLabelRenderProps) => {
  const { cx, cy, midAngle, outerRadius, percent } = props;

  if (
    typeof cx !== "number" ||
    typeof cy !== "number" ||
    typeof midAngle !== "number" ||
    typeof outerRadius !== "number" ||
    typeof percent !== "number"
  ) {
    return null;
  }

  const RADIAN = Math.PI / 180;
  const radius = outerRadius + 25;
  const x = cx + radius * Math.cos(-midAngle * RADIAN);
  const y = cy + radius * Math.sin(-midAngle * RADIAN);

  return (
    <text
      x={x}
      y={y}
      fill="hsl(var(--foreground))"
      textAnchor={x > cx ? "start" : "end"}
      dominantBaseline="central"
      className="text-xs"
    >
      {`${(percent * 100).toFixed(0)}%`}
    </text>
  );
};

export function RiskDistribution() {
  return (
    <Card className="col-span-4">
      <CardHeader>
        <CardTitle>Distribución de Riesgo</CardTitle>
        <CardDescription>
          Clasificación de transacciones por nivel de riesgo
        </CardDescription>
      </CardHeader>
      <CardContent>
        <ResponsiveContainer width="100%" height={300}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              labelLine={true}
              label={renderCustomLabel}
              outerRadius={80}
              fill="#8884d8"
              dataKey="value"
              stroke="hsl(var(--background))"
              strokeWidth={2}
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                backgroundColor: "hsl(var(--popover))",
                border: "1px solid hsl(var(--border))",
                borderRadius: "var(--radius)",
                color: "hsl(var(--popover-foreground))",
                fontSize: "14px",
                padding: "8px 12px",
              }}
            />
            <Legend />
          </PieChart>
        </ResponsiveContainer>

        <div className="mt-4 grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
          {data.map((item) => (
            <div key={item.name} className="flex items-center gap-2">
              <div
                className="h-3 w-3 rounded-full flex-shrink-0"
                style={{ backgroundColor: item.color }}
              />
              <span className="text-muted-foreground text-xs">
                {item.name}: <span className="text-foreground">{item.value}</span>
              </span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}

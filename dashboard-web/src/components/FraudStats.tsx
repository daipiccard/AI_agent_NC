import React from "react";
import { TrendingDown, TrendingUp, Shield, AlertTriangle, DollarSign, Activity } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

interface StatCardProps {
  title: string;
  value: string;
  change: string;
  trend: "up" | "down";
  icon: React.ReactNode;
  changeType?: "positive" | "negative";
}

function StatCard({ title, value, change, trend, icon, changeType = "positive" }: StatCardProps) {
  const trendColor =
    changeType === "positive"
      ? trend === "up"
        ? "text-primary"
        : "text-destructive"
      : trend === "up"
      ? "text-destructive"
      : "text-primary";

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm text-muted-foreground">{title}</CardTitle>
        <div className="h-4 w-4 text-muted-foreground">{icon}</div>
      </CardHeader>
      <CardContent>
        <div className="text-2xl">{value}</div>
        <p className="text-xs text-muted-foreground mt-1">
          <span className={trendColor}>
            {trend === "up" ? <TrendingUp className="inline h-3 w-3" /> : <TrendingDown className="inline h-3 w-3" />}{" "}
            {change}
          </span>{" "}
          vs último período
        </p>
      </CardContent>
    </Card>
  );
}

export function FraudStats() {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
      <StatCard
        title="Transacciones Procesadas"
        value="1,247,893"
        change="12.5%"
        trend="up"
        icon={<Activity className="h-4 w-4" />}
        changeType="positive"
      />
      <StatCard
        title="Fraudes Detectados"
        value="1,243"
        change="8.2%"
        trend="down"
        icon={<AlertTriangle className="h-4 w-4" />}
        changeType="positive"
      />
      <StatCard
        title="Pérdidas Evitadas"
        value="$892,450"
        change="15.3%"
        trend="up"
        icon={<DollarSign className="h-4 w-4" />}
        changeType="positive"
      />
      <StatCard
        title="Tasa de Precisión"
        value="98.7%"
        change="2.1%"
        trend="up"
        icon={<Shield className="h-4 w-4" />}
        changeType="positive"
      />
    </div>
  );
}

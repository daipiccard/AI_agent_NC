import React from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "./ui/table";
import { Button } from "./ui/button";
import { Eye, Ban, CheckCircle } from "lucide-react";

interface Transaction {
  id: string;
  user: string;
  amount: string;
  type: string;
  location: string;
  riskScore: number;
  status: "blocked" | "flagged" | "approved";
  timestamp: string;
}

const transactions: Transaction[] = [
  {
    id: "TXN-8472",
    user: "user_49281",
    amount: "$12,450.00",
    type: "Transferencia internacional",
    location: "Nigeria",
    riskScore: 94,
    status: "blocked",
    timestamp: "2025-10-02 14:23:41"
  },
  {
    id: "TXN-8471",
    user: "user_73921",
    amount: "$45,000.00",
    type: "Pago tarjeta",
    location: "Miami, USA",
    riskScore: 87,
    status: "flagged",
    timestamp: "2025-10-02 14:21:15"
  },
  {
    id: "TXN-8470",
    user: "user_12847",
    amount: "$8,200.00",
    type: "Retiro ATM",
    location: "São Paulo",
    riskScore: 82,
    status: "flagged",
    timestamp: "2025-10-02 14:18:33"
  },
  {
    id: "TXN-8469",
    user: "user_55392",
    amount: "$2,300.00",
    type: "Compra en línea",
    location: "Londres, UK",
    riskScore: 68,
    status: "flagged",
    timestamp: "2025-10-02 14:15:22"
  },
  {
    id: "TXN-8468",
    user: "user_88214",
    amount: "$890.00",
    type: "Transferencia local",
    location: "México City",
    riskScore: 91,
    status: "blocked",
    timestamp: "2025-10-02 14:12:08"
  },
  {
    id: "TXN-8467",
    user: "user_29384",
    amount: "$156.50",
    type: "Pago tarjeta",
    location: "Madrid, España",
    riskScore: 23,
    status: "approved",
    timestamp: "2025-10-02 14:10:45"
  },
];

export function TransactionsTable() {
  const getStatusBadge = (status: string) => {
    switch (status) {
      case "blocked":
        return <Badge variant="destructive">Bloqueada</Badge>;
      case "flagged":
        return <Badge variant="default">Marcada</Badge>;
      case "approved":
        return <Badge variant="secondary">Aprobada</Badge>;
      default:
        return <Badge variant="outline">{status}</Badge>;
    }
  };

  const getRiskColor = (score: number) => {
    if (score >= 80) return "text-destructive";
    if (score >= 60) return "text-primary";
    return "text-muted-foreground";
  };

  return (
    <Card className="col-span-7">
      <CardHeader>
        <CardTitle>Registro de Transacciones</CardTitle>
        <CardDescription>
          Historial de eventos para auditoría y análisis
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ID</TableHead>
              <TableHead>Usuario</TableHead>
              <TableHead>Monto</TableHead>
              <TableHead>Tipo</TableHead>
              <TableHead>Ubicación</TableHead>
              <TableHead>Riesgo</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead>Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {transactions.map((transaction) => (
              <TableRow key={transaction.id}>
                <TableCell className="font-mono text-sm">
                  {transaction.id}
                </TableCell>
                <TableCell>{transaction.user}</TableCell>
                <TableCell>{transaction.amount}</TableCell>
                <TableCell className="text-sm">{transaction.type}</TableCell>
                <TableCell className="text-sm">{transaction.location}</TableCell>
                <TableCell>
                  <span className={getRiskColor(transaction.riskScore)}>
                    {transaction.riskScore}%
                  </span>
                </TableCell>
                <TableCell>{getStatusBadge(transaction.status)}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <Button variant="ghost" size="icon" className="h-8 w-8">
                      <Eye className="h-4 w-4" />
                    </Button>
                    {transaction.status === "flagged" && (
                      <>
                        <Button variant="ghost" size="icon" className="h-8 w-8 hover:text-primary">
                          <CheckCircle className="h-4 w-4" />
                        </Button>
                        <Button variant="ghost" size="icon" className="h-8 w-8 hover:text-destructive">
                          <Ban className="h-4 w-4" />
                        </Button>
                      </>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}

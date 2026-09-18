export type Role = 'USER' | 'ADMIN';
export type LoanStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface User {
  id: number;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  expiresAt: string;
  user: User;
}

export interface Loan {
  id: number;
  userId: number;
  userEmail?: string;
  amount: number;
  termMonths: number;
  status: LoanStatus;
  requestedAt: string;
  decidedAt?: string;
}

/** Mismos límites que aplica el dominio del backend (Loan.java). El backend es quien manda. */
export const LOAN_LIMITS = {
  minAmount: 100,
  maxAmount: 1_000_000,
  minTerm: 1,
  maxTerm: 84,
} as const;

export const STATUS_LABEL: Record<LoanStatus, string> = {
  PENDING: 'pendiente',
  APPROVED: 'aprobado',
  REJECTED: 'rechazado',
};

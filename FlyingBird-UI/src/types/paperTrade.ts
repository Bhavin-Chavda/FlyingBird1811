// Mirrors backend PaperTradeDetailsResponseDto / PaperCandleResponseDto.
// BigDecimal fields serialize as JSON numbers; date fields as "yyyy-MM-dd HH:mm:ss" strings.

export interface PaperCandle {
  candleId: number;
  timeframe: string;            // "1m" / "5m" / "15m" / "1h"
  candleTime: string | null;
  openPrice: number | null;
  highPrice: number | null;
  lowPrice: number | null;
  closePrice: number | null;
  volume: number | null;
}

export interface PaperTrade {
  tradeId: number;
  timeframe: string;            // "1m" / "5m" / "15m" / "1h"
  tradeType: string;            // "BULLISH" / "BEARISH"
  tradeStatus: string;          // "OPEN" / "CLOSED"
  patternName: string;          // e.g. "DOUBLE_TOP"
  candleTime: string | null;

  tradePrice: number | null;
  stopLoss: number | null;
  initialStopLoss: number | null;
  tp1: number | null;
  tp2: number | null;
  tp3: number | null;
  tp4: number | null;

  safeTrade: boolean;
  tp1Achieved: boolean;
  tp2Achieved: boolean;
  tp3Achieved: boolean;
  tp4Achieved: boolean;

  closePrice: number | null;
  closeReason: string | null;   // "STOP_LOSS" / "TP4" / "MANUAL" / "INVALID"

  openedAt: string | null;
  closedAt: string | null;
  lastEvaluatedAt: string | null;
  createdAt: string | null;
  updatedAt: string | null;

  detectionReason: string | null;
  confidenceScore: number | null;
  breakoutLevel: number | null;
  riskAmount: number | null;
  atrAtDetection: number | null;
  patternStartTime: string | null;
  patternEndTime: string | null;

  // Crossover-state snapshot at trade creation ("BULLISH" / "BEARISH" / "NEUTRAL").
  oneMinuteCrossoverState: string | null;
  fiveMinuteCrossoverState: string | null;
  fifteenMinuteCrossoverState: string | null;
  oneHourCrossoverState: string | null;

  paperCandle: PaperCandle | null;
}

export interface Round {
  id: string;
  dynasty: string;
  locationName: string;
  historicalCity: string | null;
  modernPlace: string | null;
  geoQuery: string | null;
  yearAd: number | null;
  reignLabel: string | null;
  timeLabel: string;
  sceneType: string | null;
  knowledgeSummary: string | null;
  userPrompt: string | null;
  systemPrompt: string | null;
  imageUrl: string | null;
  imageSize: string | null;
  status: string;
  errorMessage: string | null;
  createdAt: string;
}

export interface Job {
  id: string;
  dynasty: string;
  targetCount: number;
  status: string;
  successCount: number;
  failCount: number;
  message: string | null;
  createdAt: string;
  finishedAt: string | null;
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.error || "请求失败");
  }
  return data as T;
}

export async function fetchDynasties(): Promise<string[]> {
  const data = await request<{ dynasties: string[] }>("/api/dynasties");
  return data.dynasties;
}

export interface RoundsPage {
  rows: Round[];
  total: number;
}

export async function fetchRounds(limit = 10, offset = 0): Promise<RoundsPage> {
  return request<RoundsPage>(`/api/rounds?limit=${limit}&offset=${offset}`);
}

export async function fetchJobs(limit = 10): Promise<Job[]> {
  const data = await request<{ jobs: Job[] }>(`/api/jobs?limit=${limit}`);
  return data.jobs;
}

export async function fetchJob(id: string): Promise<Job> {
  return request<Job>(`/api/jobs/${id}`);
}

export async function startGenerate(dynasty: string, count: number): Promise<{ jobId: string }> {
  return request("/api/generate", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ dynasty, count }),
  });
}

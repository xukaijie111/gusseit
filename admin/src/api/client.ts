export interface AnecdoteItem {
  id?: number;
  dynastyId: number;
  dynastyName: string;
  anecdoteName: string;
  summary: string;
  historicalPlace: string;
  modernLocation: string;
  modernCity: string;
  latitude?: number | null;
  longitude?: number | null;
}

export interface Dynasty {
  id: number;
  name: string;
}

export interface ImageItem {
  id: number;
  dynastyId: number;
  dynastyName: string;
  anecdoteName: string;
  summary: string;
  historicalPlace: string;
  modernCity: string;
  imageUrl: string;
  imageSize: string;
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(url, init);
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || "请求失败");
  return data as T;
}

export async function fetchDynasties(): Promise<Dynasty[]> {
  return request<Dynasty[]>("/api/dynasties");
}

// ==================== 草稿 ====================

export async function generateAnecdotes(dynastyId: number, count: number): Promise<{ anecdotes: AnecdoteItem[]; total: number; errors: string[] }> {
  return request("/api/admin/anecdotes/generate", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ dynasty_id: dynastyId, count }),
  });
}

export interface DraftsPage {
  rows: AnecdoteItem[];
  total: number;
  page: number;
  size: number;
}

export async function fetchDrafts(dynastyId?: number, page = 0, size = 20): Promise<DraftsPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (dynastyId) params.set("dynastyId", String(dynastyId));
  return request(`/api/admin/anecdotes/drafts?${params.toString()}`);
}

export async function approveDrafts(ids: number[]): Promise<{ approved: number; message: string }> {
  return request("/api/admin/anecdotes/drafts/approve", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids }),
  });
}

export async function rejectDrafts(ids: number[]): Promise<{ deleted: number; message: string }> {
  return request("/api/admin/anecdotes/drafts/reject", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids }),
  });
}

// ==================== 典故库 ====================

export interface PageResult {
  rows: AnecdoteItem[];
  total: number;
  page: number;
  size: number;
}

export async function fetchAnecdotes(dynastyId?: number, page = 0, size = 20): Promise<PageResult> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (dynastyId) params.set("dynastyId", String(dynastyId));
  return request(`/api/admin/anecdotes?${params.toString()}`);
}

// ==================== 题库（图片） ====================

export interface ImagesPage {
  rows: ImageItem[];
  total: number;
  page: number;
  size: number;
}

export async function fetchImages(dynastyId?: number, page = 0, size = 20): Promise<ImagesPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (dynastyId) params.set("dynastyId", String(dynastyId));
  return request(`/api/admin/anecdotes/images?${params.toString()}`);
}

export async function generateAnecdoteImages(ids: number[]): Promise<{ success: number; fail: number; progress: string[]; message: string }> {
  return request("/api/admin/anecdotes/generate-images", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids }),
  });
}

export async function deleteImages(ids: number[]): Promise<{ deleted: number; message: string }> {
  return request("/api/admin/anecdotes/images/delete", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ ids }),
  });
}

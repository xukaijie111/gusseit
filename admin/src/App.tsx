import { useCallback, useEffect, useRef, useState } from "react";
import {
  fetchDynasties,
  fetchJob,
  fetchJobs,
  fetchRounds,
  startGenerate,
  type Job,
  type Round,
} from "./api/client";
import "./App.css";

function statusClass(status: string) {
  if (status === "completed" || status === "generated") return "ok";
  if (status === "failed") return "err";
  return "pending";
}

function RoundDetailSheet({
  round,
  onClose,
}: {
  round: Round;
  onClose: () => void;
}) {
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  return (
    <>
      <div className="sheet-backdrop" onClick={onClose} aria-hidden />
      <aside className="sheet" role="dialog" aria-modal="true" aria-label="题目详情">
        <header className="sheet-header">
          <div>
            <h3>{round.dynasty} · {round.locationName}</h3>
            <p className="sheet-sub">{round.timeLabel}</p>
          </div>
          <button type="button" className="sheet-close" onClick={onClose} aria-label="关闭">
            ×
          </button>
        </header>

        <div className="sheet-body">
          {round.imageUrl && (
            <a href={round.imageUrl} target="_blank" rel="noreferrer" className="sheet-image-link">
              <img className="sheet-image" src={round.imageUrl} alt="" />
            </a>
          )}

          <dl className="meta-list">
            {round.historicalCity && (
              <>
                <dt>历史城市</dt>
                <dd>{round.historicalCity}</dd>
              </>
            )}
            {round.modernPlace && (
              <>
                <dt>现代城市</dt>
                <dd>{round.modernPlace}</dd>
              </>
            )}
            {round.geoQuery && (
              <>
                <dt>地理编码</dt>
                <dd>{round.geoQuery}</dd>
              </>
            )}
            {round.yearAd != null && (
              <>
                <dt>公元年</dt>
                <dd>{round.yearAd}</dd>
              </>
            )}
            {round.reignLabel && (
              <>
                <dt>年号</dt>
                <dd>{round.reignLabel}</dd>
              </>
            )}
            {round.sceneType && (
              <>
                <dt>历史典故</dt>
                <dd>{round.sceneType}</dd>
              </>
            )}
            <dt>状态</dt>
            <dd><span className={`tag ${statusClass(round.status)}`}>{round.status}</span></dd>
            {round.errorMessage && (
              <>
                <dt>错误</dt>
                <dd className="err-text">{round.errorMessage}</dd>
              </>
            )}
          </dl>

          {round.knowledgeSummary && (
            <section className="prompt-block">
              <h4>历史典故</h4>
              <pre>{round.knowledgeSummary}</pre>
            </section>
          )}

          <section className="prompt-block">
            <h4>出图提示词</h4>
            <pre>{round.userPrompt || "—"}</pre>
          </section>

          <section className="prompt-block">
            <h4>千问 System</h4>
            <pre>{round.systemPrompt || "—"}</pre>
          </section>
        </div>
      </aside>
    </>
  );
}

const ROUNDS_PAGE_SIZE = 10;
const JOBS_LIMIT = 10;

function JobsSidebar({ jobs }: { jobs: Job[] }) {
  return (
    <aside className="jobs-sidebar card">
      <h2>最近任务</h2>
      {jobs.length === 0 ? (
        <p className="jobs-empty">暂无任务</p>
      ) : (
        <ul className="jobs-list">
          {jobs.map((j) => (
            <li key={j.id} className="job-item">
              <div className="job-head">
                <span className="job-dynasty">{j.dynasty}</span>
                <span className={`tag ${statusClass(j.status)}`}>{j.status}</span>
              </div>
              <div className="job-meta">
                目标 {j.targetCount} · 成功 {j.successCount} / 失败 {j.failCount}
              </div>
              {j.message && <p className="job-msg">{j.message}</p>}
              <time className="job-time">{new Date(j.createdAt).toLocaleString()}</time>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
}

export default function App() {
  const [dynasties, setDynasties] = useState<string[]>([]);
  const [dynasty, setDynasty] = useState("唐");
  const [count, setCount] = useState(5);
  const [status, setStatus] = useState("就绪");
  const [generating, setGenerating] = useState(false);
  const [jobs, setJobs] = useState<Job[]>([]);
  const [rounds, setRounds] = useState<Round[]>([]);
  const [roundsTotal, setRoundsTotal] = useState(0);
  const [roundsPage, setRoundsPage] = useState(0);
  const [selectedRound, setSelectedRound] = useState<Round | null>(null);
  const pollRef = useRef<number | null>(null);

  const loadRounds = useCallback(async (page: number) => {
    const data = await fetchRounds(ROUNDS_PAGE_SIZE, page * ROUNDS_PAGE_SIZE);
    setRounds(data.rows);
    setRoundsTotal(data.total);
  }, []);

  const refresh = useCallback(async () => {
    const j = await fetchJobs(JOBS_LIMIT);
    setJobs(j);
    await loadRounds(roundsPage);
  }, [loadRounds, roundsPage]);

  const totalRoundPages = Math.max(1, Math.ceil(roundsTotal / ROUNDS_PAGE_SIZE));

  const goRoundsPage = (page: number) => {
    setRoundsPage(Math.min(Math.max(page, 0), totalRoundPages - 1));
  };

  useEffect(() => {
    fetchDynasties().then((list) => {
      setDynasties(list);
      if (list.length) setDynasty(list.includes("唐") ? "唐" : list[0]);
    });
    fetchJobs(JOBS_LIMIT).then(setJobs);
  }, []);

  useEffect(() => {
    loadRounds(roundsPage);
  }, [roundsPage, loadRounds]);

  useEffect(() => {
    return () => {
      if (pollRef.current) window.clearTimeout(pollRef.current);
    };
  }, []);

  const pollJob = (jobId: string) => {
    const tick = async () => {
      try {
        const job = await fetchJob(jobId);
        setStatus(job.message || job.status);
        await refresh();

        if (job.status === "running" || job.status === "pending") {
          pollRef.current = window.setTimeout(tick, 3000);
        } else {
          setGenerating(false);
          setStatus(job.message || "完成");
          setRoundsPage(0);
          await loadRounds(0);
        }
      } catch (e) {
        setGenerating(false);
        setStatus(e instanceof Error ? e.message : "轮询失败");
      }
    };
    tick();
  };

  const handleGenerate = async () => {
    setGenerating(true);
    setStatus("提交中...");
    if (pollRef.current) window.clearTimeout(pollRef.current);

    try {
      const { jobId } = await startGenerate(dynasty, count);
      setStatus("任务已启动，生成中（每张约 30–60 秒）...");
      pollJob(jobId);
    } catch (e) {
      setGenerating(false);
      setStatus(e instanceof Error ? e.message : "启动失败");
    }
  };

  return (
    <div className="wrap">
      <header>
        <h1>Guseeit 出题台</h1>
        <p className="sub">Java 后端 · 选择朝代与数量，自动去重并入库</p>
      </header>

      <div className="main-layout">
        <div className="main-col">
          <section className="card">
            <div className="form-row">
              <label>
                朝代
                <select value={dynasty} onChange={(e) => setDynasty(e.target.value)} disabled={generating}>
                  {dynasties.map((d) => (
                    <option key={d} value={d}>{d}</option>
                  ))}
                </select>
              </label>
              <label>
                张数
                <input
                  type="number"
                  min={1}
                  max={20}
                  value={count}
                  onChange={(e) => setCount(Number(e.target.value))}
                  disabled={generating}
                />
              </label>
              <button type="button" onClick={handleGenerate} disabled={generating}>
                {generating ? "生成中..." : "开始生成"}
              </button>
            </div>
            <div className="status">{status}</div>
          </section>

          <section className="card">
            <div className="card-head">
              <h2>题库</h2>
              <span className="card-meta">按生成时间倒序 · 共 {roundsTotal} 条</span>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>图</th>
                    <th>朝代</th>
                    <th>地点</th>
                    <th>历史时间</th>
                    <th>生成时间</th>
                    <th>状态</th>
                  </tr>
                </thead>
                <tbody>
                  {rounds.length === 0 ? (
                    <tr><td colSpan={6}>暂无数据</td></tr>
                  ) : (
                    rounds.map((r) => (
                      <tr
                        key={r.id}
                        className="round-row"
                        onClick={() => setSelectedRound(r)}
                      >
                        <td>
                          {r.imageUrl ? (
                            <a
                              href={r.imageUrl}
                              target="_blank"
                              rel="noreferrer"
                              onClick={(e) => e.stopPropagation()}
                            >
                              <img className="thumb" src={r.imageUrl} alt="" />
                            </a>
                          ) : "—"}
                        </td>
                        <td>{r.dynasty}</td>
                        <td>
                          {r.locationName}
                          {r.modernPlace && <small>{r.modernPlace}</small>}
                        </td>
                        <td>{r.timeLabel}</td>
                        <td>{new Date(r.createdAt).toLocaleString()}</td>
                        <td><span className={`tag ${statusClass(r.status)}`}>{r.status}</span></td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
            {roundsTotal > 0 && (
              <div className="pagination">
                <button
                  type="button"
                  disabled={roundsPage <= 0}
                  onClick={() => goRoundsPage(roundsPage - 1)}
                >
                  上一页
                </button>
                <span>
                  第 {roundsPage + 1} / {totalRoundPages} 页
                </span>
                <button
                  type="button"
                  disabled={roundsPage >= totalRoundPages - 1}
                  onClick={() => goRoundsPage(roundsPage + 1)}
                >
                  下一页
                </button>
              </div>
            )}
          </section>
        </div>

        <JobsSidebar jobs={jobs} />
      </div>

      {selectedRound && (
        <RoundDetailSheet round={selectedRound} onClose={() => setSelectedRound(null)} />
      )}
    </div>
  );
}

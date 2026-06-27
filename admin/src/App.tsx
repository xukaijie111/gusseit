import { useCallback, useEffect, useState } from "react";
import {
  fetchDynasties,
  fetchAnecdotes,
  fetchDrafts,
  fetchImages,
  generateAnecdotes,
  generateAnecdoteImages,
  approveDrafts,
  rejectDrafts,
  deleteImages,
  type AnecdoteItem,
  type Dynasty,
  type ImageItem,
} from "./api/client";
import "./App.css";

type TabKey = "draft" | "list" | "bank";

/* ========================= Tab 1: 典故草稿 ========================= */

function DraftTab() {
  const PAGE_SIZE = 20;
  const [dynasties, setDynasties] = useState<Dynasty[]>([]);
  const [dynastyId, setDynastyId] = useState(0);
  const [dynastyFilter, setDynastyFilter] = useState(0);
  const [count, setCount] = useState(10);
  const [loading, setLoading] = useState(false);
  const [genLoading, setGenLoading] = useState(false);
  const [status, setStatus] = useState("");
  const [rows, setRows] = useState<AnecdoteItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [checked, setChecked] = useState<Set<number>>(new Set());

  useEffect(() => {
    fetchDynasties().then(list => {
      setDynasties(list);
      if (list.length) setDynastyId(list[0].id);
    });
  }, []);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const res = await fetchDrafts(dynastyFilter || undefined, p, PAGE_SIZE);
      setRows(res.rows); setTotal(res.total); setChecked(new Set());
    } catch (e) { setStatus(e instanceof Error ? e.message : "加载失败"); }
    finally { setLoading(false); }
  }, [dynastyFilter]);

  useEffect(() => { setPage(0); load(0); }, [dynastyFilter, load]);
  useEffect(() => { load(page); }, [page, load]);

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));

  const handleGenerate = async () => {
    setGenLoading(true); setStatus("千问生成中...");
    try {
      const res = await generateAnecdotes(dynastyId, count);
      let msg = `生成完成，获得 ${res.total} 条新草稿`;
      if (res.errors && res.errors.length > 0) {
        msg += `，${res.errors.length} 条失败`;
        setStatus(msg + "\n" + res.errors.join("\n"));
      } else {
        setStatus(msg);
      }
      setPage(0); load(0);
    } catch (e) { setStatus(e instanceof Error ? e.message : "生成失败"); }
    finally { setGenLoading(false); }
  };

  const toggle = (id: number) => setChecked(prev => {
    const next = new Set(prev);
    if (next.has(id)) next.delete(id); else next.add(id);
    return next;
  });
  const toggleAll = () => {
    const ids = rows.map(r => r.id!);
    if (ids.length > 0 && ids.every(id => checked.has(id))) setChecked(new Set());
    else setChecked(new Set(ids));
  };
  const handleApprove = async () => {
    if (!checked.size) return; setStatus("入库中...");
    try { const res = await approveDrafts(Array.from(checked)); setStatus(res.message); setChecked(new Set()); load(page); }
    catch (e) { setStatus(e instanceof Error ? e.message : "入库失败"); }
  };
  const handleReject = async () => {
    if (!checked.size) return; setStatus("删除中...");
    try { const res = await rejectDrafts(Array.from(checked)); setStatus(res.message); setChecked(new Set()); load(page); }
    catch (e) { setStatus(e instanceof Error ? e.message : "删除失败"); }
  };

  return (
    <div>
      <section className="card">
        <div className="form-row">
          <label>朝代
            <select value={dynastyId} onChange={e => setDynastyId(Number(e.target.value))} disabled={genLoading}>
              {dynasties.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </label>
          <label>数量
            <input type="number" min={1} max={20} value={count} onChange={e => setCount(Number(e.target.value))} disabled={genLoading} />
          </label>
          <button className="btn-gold" onClick={handleGenerate} disabled={genLoading}>{genLoading ? "生成中..." : "千问生成"}</button>
        </div>
        {status && <div className="status">{status}</div>}
      </section>

      <section className="card">
        <div className="card-head">
          <h2>草稿箱</h2>
          <span className="card-meta">共 {total} 条</span>
          <div className="card-actions">
            <select value={dynastyFilter} onChange={e => setDynastyFilter(Number(e.target.value))}>
              <option value={0}>全部朝代</option>
              {dynasties.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            <button className="btn-gold" onClick={handleApprove} disabled={!checked.size}>审批入库 ({checked.size})</button>
            <button className="btn-secondary" onClick={handleReject} disabled={!checked.size}>删除 ({checked.size})</button>
          </div>
        </div>
        {loading ? <p className="loading-text">加载中...</p> : rows.length === 0 ? <p className="loading-text">暂无草稿</p> : (
          <>
            <div className="table-wrap"><table>
              <thead><tr>
                <th style={{width:40}}><input type="checkbox" onChange={toggleAll} /></th>
                <th>典故</th><th>朝代</th><th>简述</th><th>古时地名</th><th>现代所属地</th><th>现代市</th><th style={{width:140}}>操作</th>
              </tr></thead>
              <tbody>
                {rows.map(r => (
                  <tr key={r.id} className={checked.has(r.id!) ? "row-selected" : ""}>
                    <td><input type="checkbox" checked={checked.has(r.id!)} onChange={() => toggle(r.id!)} /></td>
                    <td className="cell-bold">{r.anecdoteName}</td>
                    <td>{r.dynastyName}</td>
                    <td className="cell-summary">{r.summary}</td>
                    <td>{r.historicalPlace}</td>
                    <td className="cell-location">{r.modernLocation}</td>
                    <td>{r.modernCity}</td>
                    <td className="cell-actions">
                      <button className="btn-gold btn-sm" onClick={() => approveDrafts([r.id!]).then(res => {setStatus(res.message);load(page);}).catch(e => setStatus(e.message))}>审批</button>
                      <button className="btn-secondary btn-sm" onClick={() => rejectDrafts([r.id!]).then(res => {setStatus(res.message);load(page);}).catch(e => setStatus(e.message))}>删除</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table></div>
            {total > PAGE_SIZE && <div className="pagination"><button disabled={page<=0} onClick={()=>setPage(page-1)}>上一页</button><span>第 {page+1}/{totalPages} 页</span><button disabled={page>=totalPages-1} onClick={()=>setPage(page+1)}>下一页</button></div>}
          </>
        )}
      </section>
    </div>
  );
}

/* ========================= Tab 2: 典故列表 ========================= */

function ListTab() {
  const PAGE_SIZE = 20;
  const [rows, setRows] = useState<AnecdoteItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [dynastyId, setDynastyId] = useState(0);
  const [dynasties, setDynasties] = useState<Dynasty[]>([]);
  const [checked, setChecked] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);
  const [genLoading, setGenLoading] = useState(false);
  const [status, setStatus] = useState("");

  useEffect(() => { fetchDynasties().then(setDynasties); }, []);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try { const res = await fetchAnecdotes(dynastyId || undefined, p, PAGE_SIZE); setRows(res.rows); setTotal(res.total); setChecked(new Set()); }
    catch (e) { setStatus(e instanceof Error ? e.message : "加载失败"); }
    finally { setLoading(false); }
  }, [dynastyId]);
  useEffect(() => { setPage(0); load(0); }, [dynastyId, load]);
  useEffect(() => { load(page); }, [page, load]);

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const toggle = (id: number) => setChecked(prev => { const n=new Set(prev); if(n.has(id))n.delete(id);else n.add(id);return n; });
  const toggleAll = () => { const ids=rows.map(r=>r.id!); if(ids.length>0&&ids.every(id=>checked.has(id)))setChecked(new Set());else setChecked(new Set(ids)); };
  const handleGenerate = async () => {
    if(!checked.size)return; setGenLoading(true); setStatus(`正在生成 ${checked.size} 张图片...`);
    try { const res=await generateAnecdoteImages(Array.from(checked)); setStatus(`完成：成功 ${res.success}，失败 ${res.fail}`); setChecked(new Set()); }
    catch(e){ setStatus(e instanceof Error?e.message:"生成失败"); }
    finally{ setGenLoading(false); }
  };

  return (
    <div>
      <section className="card">
        <div className="form-row">
          <label>朝代
            <select value={dynastyId} onChange={e => setDynastyId(Number(e.target.value))}>
              <option value={0}>全部</option>
              {dynasties.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </label>
          <button className="btn-gold" onClick={handleGenerate} disabled={genLoading||!checked.size}>{genLoading?"生成中...":`生成选中图片 (${checked.size})`}</button>
        </div>
        {status && <div className="status">{status}</div>}
      </section>
      <section className="card">
        <div className="card-head"><h2>已入库典故</h2><span className="card-meta">共 {total} 条</span></div>
        {loading ? <p className="loading-text">加载中...</p> : (
          <>
            <div className="table-wrap"><table>
              <thead><tr><th style={{width:40}}><input type="checkbox" onChange={toggleAll} /></th><th>典故</th><th>朝代</th><th>简述</th><th>古时地名</th><th>现代市</th></tr></thead>
              <tbody>
                {rows.map(r=>(<tr key={r.id} className={checked.has(r.id!)?"row-selected":""}><td><input type="checkbox" checked={checked.has(r.id!)} onChange={()=>toggle(r.id!)} /></td><td className="cell-bold">{r.anecdoteName}</td><td>{r.dynastyName}</td><td className="cell-summary">{r.summary}</td><td>{r.historicalPlace}</td><td>{r.modernCity}</td></tr>))}
              </tbody>
            </table></div>
            {total>PAGE_SIZE&&<div className="pagination"><button disabled={page<=0} onClick={()=>setPage(page-1)}>上一页</button><span>第 {page+1}/{totalPages} 页</span><button disabled={page>=totalPages-1} onClick={()=>setPage(page+1)}>下一页</button></div>}
          </>
        )}
      </section>
    </div>
  );
}

/* ========================= Tab 3: 题库 ========================= */

function BankTab() {
  const PAGE_SIZE = 20;
  const [rows, setRows] = useState<ImageItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [dynastyId, setDynastyId] = useState(0);
  const [dynasties, setDynasties] = useState<Dynasty[]>([]);
  const [checked, setChecked] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState("");

  useEffect(() => { fetchDynasties().then(setDynasties); }, []);
  const load = useCallback(async (p: number) => {
    setLoading(true);
    try { const res = await fetchImages(dynastyId || undefined, p, PAGE_SIZE); setRows(res.rows); setTotal(res.total); setChecked(new Set()); }
    catch (e) { setStatus(e instanceof Error ? e.message : "加载失败"); }
    finally { setLoading(false); }
  }, [dynastyId]);
  useEffect(() => { setPage(0); load(0); }, [dynastyId, load]);
  useEffect(() => { load(page); }, [page, load]);

  const totalPages = Math.max(1, Math.ceil(total / PAGE_SIZE));
  const toggle = (id: number) => setChecked(prev => { const n = new Set(prev); if (n.has(id)) n.delete(id); else n.add(id); return n; });
  const toggleAll = () => { const ids = rows.map(r => r.id); if (ids.length > 0 && ids.every(id => checked.has(id))) setChecked(new Set()); else setChecked(new Set(ids)); };
  const handleDelete = async () => {
    if (!checked.size) return; setStatus("删除中...");
    try { const res = await deleteImages(Array.from(checked)); setStatus(res.message); setChecked(new Set()); load(page); }
    catch (e) { setStatus(e instanceof Error ? e.message : "删除失败"); }
  };

  return (
    <div>
      <section className="card">
        <div className="form-row">
          <label>朝代
            <select value={dynastyId} onChange={e => setDynastyId(Number(e.target.value))}>
              <option value={0}>全部</option>
              {dynasties.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
          </label>
        </div>
        {status && <div className="status">{status}</div>}
      </section>
      <section className="card">
        <div className="card-head">
          <h2>题库（小程序出题源）</h2>
          <span className="card-meta">共 {total} 条</span>
          <div className="card-actions">
            <button className="btn-secondary" onClick={handleDelete} disabled={!checked.size}>删除 ({checked.size})</button>
          </div>
        </div>
        {loading ? <p className="loading-text">加载中...</p> : (
          <>
            <div className="table-wrap"><table>
              <thead><tr>
                <th style={{width:40}}><input type="checkbox" onChange={toggleAll} /></th>
                <th>典故</th><th>朝代</th><th>简述</th><th>古时地名</th><th>现代市</th><th style={{width:72}}>图片</th><th style={{width:60}}>操作</th>
              </tr></thead>
              <tbody>
                {rows.map(r => (<tr key={r.id} className={checked.has(r.id) ? "row-selected" : ""}>
                  <td><input type="checkbox" checked={checked.has(r.id)} onChange={() => toggle(r.id)} /></td>
                  <td className="cell-bold">{r.anecdoteName}</td>
                  <td>{r.dynastyName}</td>
                  <td className="cell-summary">{r.summary}</td>
                  <td>{r.historicalPlace}</td>
                  <td>{r.modernCity}</td>
                  <td>{r.imageUrl ? <a href={r.imageUrl} target="_blank" rel="noreferrer"><img className="thumb" src={r.imageUrl} alt="" /></a> : <span className="tag pending">—</span>}</td>
                  <td className="cell-actions"><button className="btn-secondary btn-sm" onClick={() => deleteImages([r.id]).then(res => { setStatus(res.message); load(page); }).catch(e => setStatus(e.message))}>删除</button></td>
                </tr>))}
              </tbody>
            </table></div>
            {total > PAGE_SIZE && <div className="pagination"><button disabled={page <= 0} onClick={() => setPage(page - 1)}>上一页</button><span>第 {page + 1}/{totalPages} 页</span><button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>下一页</button></div>}
          </>
        )}
      </section>
    </div>
  );
}

export default function App() {
  const [tab, setTab] = useState<TabKey>("draft");
  const labels: Record<TabKey, string> = { draft: "典故草稿", list: "典故列表", bank: "题库" };
  return (
    <div className="wrap">
      <header><h1>Guseeit 出题台</h1><p className="sub">草稿 · 典故 · 题库</p></header>
      <nav className="tabs">
        {(["draft","list","bank"] as TabKey[]).map(k => (
          <button key={k} className={`tab-btn ${tab===k?"active":""}`} onClick={()=>setTab(k)}>{labels[k]}</button>
        ))}
      </nav>
      <div className="tab-content">
        {tab==="draft"&&<DraftTab/>}{tab==="list"&&<ListTab/>}{tab==="bank"&&<BankTab/>}
      </div>
    </div>
  );
}

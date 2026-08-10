from __future__ import annotations
import re
from dataclasses import dataclass, replace
from datetime import date, datetime
from pathlib import Path
from typing import Iterable
from .hwpx_reader import HwpxReader, HwpxTable

_DATE_HEADING_RE = re.compile(r"(?P<month>\d{1,2})월\s*(?P<day>\d{1,2})일\s*(?P<weekday>[월화수목금토일])요일")
_FILE_YEAR_RE = re.compile(r"(?P<year>20\d{2})")
_TIME_RE = re.compile(r"(?P<start>\d{1,2}:\d{2})\s*~\s*(?P<end>\d{1,2}:\d{2})")
_WEEKDAYS = {"월":"MONDAY","화":"TUESDAY","수":"WEDNESDAY","목":"THURSDAY","금":"FRIDAY","토":"SATURDAY","일":"SUNDAY"}

@dataclass(frozen=True)
class ClassRecord:
    date: str
    weekday: str
    period: int
    subject: str
    start: str
    end: str
    room: str = ""
    confidence: str = "high"

def _date_info(table: HwpxTable, source_path: Path) -> tuple[str,str]:
    match = _DATE_HEADING_RE.search(table.date_heading)
    if not match: raise ValueError(f"Unsupported date heading: {table.date_heading}")
    ym = _FILE_YEAR_RE.search(source_path.name)
    year = int(ym.group('year')) if ym else datetime.now().year
    value = date(year, int(match.group('month')), int(match.group('day')))
    return value.isoformat(), _WEEKDAYS[match.group('weekday')]

def _period_for_row(table: HwpxTable, row: int):
    candidates=[]
    for cell in table.cells:
        if cell.row != row or not cell.texts: continue
        p=cell.texts[0].strip()
        if not p.isdigit() or not 1 <= int(p) <= 12: continue
        tt=next((t for t in cell.texts[1:] if _TIME_RE.fullmatch(t.strip())),None)
        if not tt: continue
        m=_TIME_RE.fullmatch(tt.strip()); candidates.append((cell.col,int(p),m.group('start'),m.group('end')))
    if not candidates: return None
    _,p,s,e=max(candidates,key=lambda x:x[0]); return p,s,e

def _is_teacher_cell(texts: tuple[str,...], full_name: str, short_name: str) -> bool:
    return len(texts)>=2 and texts[1].strip() in (full_name,short_name)

def extract_teacher_classes(path: Path|str, full_name: str='강성호', short_name: str='강성') -> list[ClassRecord]:
    source=Path(path); rows=[]
    for table in HwpxReader(source).timetable_tables():
        day,weekday=_date_info(table,source)
        for cell in table.cells:
            if not _is_teacher_cell(cell.texts,full_name,short_name): continue
            pd=_period_for_row(table,cell.row)
            if pd is None: continue
            period,start,end=pd; subject=cell.texts[0].strip(); room=cell.texts[2].strip() if len(cell.texts)>=3 else ''
            rows.append(ClassRecord(day,weekday,period,subject,start,end,room,'high' if room else 'review'))
    return merge_split_periods(rows)

def merge_split_periods(records: Iterable[ClassRecord]) -> list[ClassRecord]:
    merged={}
    for item in records:
        key=(item.date,item.period,item.subject,item.room); cur=merged.get(key)
        if cur is None: merged[key]=item; continue
        merged[key]=replace(cur,start=min(cur.start,item.start),end=max(cur.end,item.end),confidence='review' if 'review' in (cur.confidence,item.confidence) else 'high')
    return sorted(merged.values(),key=lambda x:(x.date,x.start,x.period,x.subject))

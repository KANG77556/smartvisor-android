from __future__ import annotations
import json
from dataclasses import asdict
from datetime import datetime, timezone
from pathlib import Path
from .timetable_normalizer import ClassRecord

def build_school_work_data(source_file: str, teacher: str, classes: list[ClassRecord], tasks: list[dict]) -> dict:
    ordered=sorted(classes,key=lambda x:(x.date,x.start,x.period,x.subject))
    for c in ordered:
        if not all((c.date,c.period,c.subject,c.start,c.end)): raise ValueError('class record missing required field')
    for t in tasks:
        if not all(k in t for k in ('id','date','title','priority','completed')): raise ValueError('task missing required field')
    return {'schemaVersion':1,'generatedAt':datetime.now(timezone.utc).astimezone().isoformat(timespec='seconds'),'source':{'type':'hwpx','fileName':source_file},'teacher':teacher,'classes':[asdict(c) for c in ordered],'tasks':tasks}

def export_school_work_data(path: str|Path, source_file: str, teacher: str, classes: list[ClassRecord], tasks: list[dict]) -> Path:
    target=Path(path); data=build_school_work_data(source_file,teacher,classes,tasks)
    target.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8'); return target

from __future__ import annotations
from dataclasses import replace
from pathlib import Path
from .school_work_export import export_school_work_data
from .timetable_normalizer import ClassRecord, extract_teacher_classes

class AppModel:
    def __init__(self):
        self.source_file=''; self.teacher='강성호'; self.classes=[]; self.tasks=[]
    def load_hwpx(self,path: str|Path):
        new_classes=extract_teacher_classes(path,self.teacher,'강성')
        self.source_file=Path(path).name; self.classes=new_classes
    def edit_class(self,index:int,**changes): self.classes[index]=replace(self.classes[index],**changes)
    def add_task(self,task:dict): self.tasks.append(dict(task))
    def remove_task(self,index:int): self.tasks.pop(index)
    def export(self,path: str|Path): return export_school_work_data(path,self.source_file,self.teacher,self.classes,self.tasks)

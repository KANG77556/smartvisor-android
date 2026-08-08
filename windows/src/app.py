from __future__ import annotations
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
from .app_model import AppModel

class App(tk.Tk):
    def __init__(self):
        super().__init__(); self.title('학교 업무 워치 연동'); self.geometry('900x560'); self.model=AppModel(); self._build()
    def _build(self):
        top=ttk.Frame(self,padding=10); top.pack(fill='x')
        ttk.Button(top,text='HWPX 열기',command=self.open_hwpx).pack(side='left')
        ttk.Button(top,text='워치 데이터 저장',command=self.save_json).pack(side='left',padx=8)
        self.status=ttk.Label(top,text='HWPX를 선택하세요'); self.status.pack(side='left',padx=12)
        self.tree=ttk.Treeview(self,columns=('date','period','subject','start','end','room'),show='headings',height=14)
        for c,t,w in [('date','날짜',110),('period','교시',60),('subject','과목',120),('start','시작',80),('end','종료',80),('room','교실',140)]: self.tree.heading(c,text=t); self.tree.column(c,width=w,anchor='center')
        self.tree.pack(fill='both',expand=True,padx=10,pady=8)
        task=ttk.Frame(self,padding=10); task.pack(fill='x'); ttk.Label(task,text='오늘 할 일').pack(side='left')
        self.task_entry=ttk.Entry(task); self.task_entry.pack(side='left',fill='x',expand=True,padx=8); ttk.Button(task,text='추가',command=self.add_task).pack(side='left')
        self.task_list=tk.Listbox(self,height=4); self.task_list.pack(fill='x',padx=10,pady=(0,10))
    def open_hwpx(self):
        p=filedialog.askopenfilename(filetypes=[('HWPX','*.hwpx')])
        if not p:return
        try:self.model.load_hwpx(p); self.refresh(); self.status.config(text=f'{len(self.model.classes)}개 수업 추출')
        except Exception as e:messagebox.showerror('가져오기 오류',str(e))
    def refresh(self):
        for i in self.tree.get_children():self.tree.delete(i)
        for r in self.model.classes:self.tree.insert('', 'end',values=(r.date,r.period,r.subject,r.start,r.end,r.room),tags=(r.confidence,))
        self.tree.tag_configure('review',background='#fff1b8')
    def add_task(self):
        title=self.task_entry.get().strip()
        if not title:return
        from datetime import date
        t={'id':f'task-{len(self.model.tasks)+1}','date':date.today().isoformat(),'title':title,'priority':1,'completed':False}; self.model.add_task(t); self.task_list.insert('end',title); self.task_entry.delete(0,'end')
    def save_json(self):
        p=filedialog.asksaveasfilename(defaultextension='.json',initialfile='school_work_data.json',filetypes=[('JSON','*.json')])
        if not p:return
        try:self.model.export(p); messagebox.showinfo('저장 완료','스마트폰으로 전송할 JSON 파일을 저장했습니다.')
        except Exception as e:messagebox.showerror('저장 오류',str(e))

if __name__=='__main__': App().mainloop()

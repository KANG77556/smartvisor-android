from __future__ import annotations

import re
import zipfile
from dataclasses import dataclass
from pathlib import Path
from xml.etree import ElementTree as ET

_DATE_RE = re.compile(r'\d{1,2}월\s+\d{1,2}일\s+(?:월|화|수|목|금|토|일)요일')


def _local_name(elem: ET.Element) -> str:
    return elem.tag.split('}')[-1]


def _texts_without_nested_tables(elem: ET.Element) -> tuple[str, ...]:
    texts: list[str] = []

    def visit(node: ET.Element, *, root: bool = False) -> None:
        if not root and _local_name(node) == 'tbl':
            return
        if _local_name(node) == 't' and node.text and node.text.strip():
            texts.append(node.text.strip())
        for child in list(node):
            visit(child)

    visit(elem, root=True)
    return tuple(texts)


@dataclass(frozen=True)
class HwpxCell:
    row: int
    col: int
    row_span: int
    col_span: int
    texts: tuple[str, ...]


@dataclass(frozen=True)
class HwpxTable:
    date_heading: str
    row_count: int
    col_count: int
    cells: tuple[HwpxCell, ...]


class HwpxReader:
    def __init__(self, path: Path):
        self.path = Path(path)

    def section_names(self) -> list[str]:
        with zipfile.ZipFile(self.path) as zf:
            return sorted(
                name for name in zf.namelist()
                if re.fullmatch(r'Contents/section\d+\.xml', name)
            )

    def _section_roots(self) -> list[ET.Element]:
        with zipfile.ZipFile(self.path) as zf:
            return [ET.fromstring(zf.read(name)) for name in self.section_names()]

    def date_headings(self) -> list[str]:
        found: list[str] = []
        for root in self._section_roots():
            for elem in root.iter():
                if _local_name(elem) != 't' or not elem.text:
                    continue
                for match in _DATE_RE.findall(elem.text):
                    if match not in found:
                        found.append(match)
        return found

    def timetable_tables(self) -> list[HwpxTable]:
        tables: list[HwpxTable] = []
        for root in self._section_roots():
            for elem in root.iter():
                if _local_name(elem) != 'tbl' or elem.attrib.get('noAdjust') != '1':
                    continue
                cells = self._direct_table_cells(elem)
                date_heading = next(
                    (
                        text
                        for cell in cells
                        for text in cell.texts
                        if _DATE_RE.fullmatch(text)
                    ),
                    None,
                )
                if date_heading is None:
                    continue
                tables.append(
                    HwpxTable(
                        date_heading=date_heading,
                        row_count=int(elem.attrib['rowCnt']),
                        col_count=int(elem.attrib['colCnt']),
                        cells=tuple(cells),
                    )
                )
        return tables

    @staticmethod
    def _direct_table_cells(table: ET.Element) -> list[HwpxCell]:
        cells: list[HwpxCell] = []
        for row in list(table):
            if _local_name(row) != 'tr':
                continue
            for cell in list(row):
                if _local_name(cell) != 'tc':
                    continue
                addr = next((e for e in cell.iter() if _local_name(e) == 'cellAddr'), None)
                span = next((e for e in cell.iter() if _local_name(e) == 'cellSpan'), None)
                if addr is None or span is None:
                    continue
                cells.append(
                    HwpxCell(
                        row=int(addr.attrib['rowAddr']),
                        col=int(addr.attrib['colAddr']),
                        row_span=int(span.attrib['rowSpan']),
                        col_span=int(span.attrib['colSpan']),
                        texts=_texts_without_nested_tables(cell),
                    )
                )
        return cells

package org.thighfill.ambi;

import org.thighfill.ambi.data.AmbiDocument;
import org.thighfill.ambi.data.Page;

import java.util.Arrays;

public class PageViewer {

    private ScalingLabel _leftLbl, _rightLbl;

    public PageViewer(ScalingLabel leftPage, ScalingLabel rightPage) {
        _leftLbl = leftPage;
        _rightLbl = rightPage;
    }

    private AmbiDocument currDoc = null;
    private int currPage = 0;

    public void setDocument(AmbiDocument document) {
        currDoc = document;
    }

    public void setPage(int page) {
        currPage = page;
        int[] pages = getPages();
        Page left = getPage(pages[0]), right = pages.length > 1 ? getPage(pages[1]) : null;
        if (currDoc.isRightToLeft()) {
            Page tmp = left;
            left = right;
            right = tmp;
        }
        left.addToLabel(_leftLbl);
        right.addToLabel(_rightLbl);
    }

    public int nextPage() {
        int[] pages = getPages();
        return Math.min(currDoc.getPages().size(), pages[pages.length == 1 ? 0 : 1] + 1);
    }

    public int prevPage() {
        int[] pages = getPages();
        return Math.max(0, pages[0] - 1);
    }

    private int[] getPages() {
        if (!currDoc.isTwoPageMode()) {
            return new int[] { currPage };
        }
        int offset = 0;
        if (currDoc.isFirstPageAlone()) {
            if (currPage == 0) {
                return new int[] { currPage };
            }
            offset = 1;
        }
        int[] res = { currPage, -1 };
        if ((currPage - offset) % 2 == 0) {
            res[1] = currPage + 1;
        }
        else {
            res[1] = currPage - 1;
        }
        Arrays.sort(res);
        return res;
    }

    private Page getPage(int page) {
        return currDoc.getPages().get(page);
    }
}

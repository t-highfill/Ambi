package org.thighfill.ambi.data;

import org.thighfill.ambi.ScalingLabel;

import java.awt.image.BufferedImage;

public class PDFPage extends Page {

    private Integer _fixedPageNum;
    private int _pageNum;

    protected PDFPage(AmbiDocument doc, Bean bean, int pageNum) {
        super(doc, bean);
        _fixedPageNum = bean.pageNum;
        _pageNum = _fixedPageNum != null ? _fixedPageNum : pageNum;
    }

    @Override
    public void addToLabel(ScalingLabel label) {
        label.setText("");
        label.setMaster(getIcon());
    }

    @Override
    public BufferedImage getIcon() {
        return getDoc().getPdfCache().get(_pageNum);
    }

    @Override
    protected Bean toBean() {
        Bean bean = super.toBean();
        bean.pageNum = _fixedPageNum;
        return bean;
    }
}

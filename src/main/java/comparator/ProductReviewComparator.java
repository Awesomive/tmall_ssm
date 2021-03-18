package comparator;


import java.util.Comparator;

import com.how2java.tmall.pojo.Product;

/*人气比较器
把 评价数量多的放前面*/
public class ProductReviewComparator implements Comparator<Product> {

	@Override
	public int compare(Product p1, Product p2) {
		return p2.getReviewCount()-p1.getReviewCount();
	}

}

/**
 * Created by $maoshuwu on 2020/12/7.
 */
public class QuickSort1 {
    public static void quickSort(int[] a, int l, int r) {
        if(l<r) {
            int i, j, x;
            i = l;
            j = r;
            x = a[i];
            while (l<r){
                while (l<r && a[j]>x){
                    j--;

                }
                if(l<r){
                    a[i++]=a[j];
                }

                while (i < j && a[i] < x){
                    i++;
                }
                if (l<r){
                    a[j--] = a[i];
                }
            }
            a[i]=x;
            quickSort (a,l,i-1);
                    quickSort(a,i+1,r);


        }

    }

    public static void main(String[] args) {
        int i;
        int a[] = {30,40,60,10,20,50};

        System.out.printf("before sort:");
        for (i=0; i<a.length; i++)
            System.out.printf("%d ", a[i]);
        System.out.printf("\n");

        quickSort(a, 0, a.length-1);

        System.out.printf("after  sort:");
        for (i=0; i<a.length; i++)
            System.out.printf("%d ", a[i]);
        System.out.printf("\n");
    }


}
